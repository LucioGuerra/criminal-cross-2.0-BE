package org.athlium.auth.infrastructure.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.exception.AuthenticationException;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.DecodedToken;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.users.domain.model.Role;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * JAX-RS filter that intercepts requests and validates Firebase ID tokens.
 * Populates the SecurityContext with the authenticated user.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class FirebaseAuthFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(FirebaseAuthFilter.class);

    @Context
    ResourceInfo resourceInfo;

    @Inject
    TokenValidator tokenValidator;

    @Inject
    UserProvider userProvider;

    @Inject
    SecurityContext securityContext;

    @ConfigProperty(name = "auth.dev-bypass.enabled", defaultValue = "false")
    boolean authBypassEnabled;

    @ConfigProperty(name = "auth.dev-bypass.firebase-uid", defaultValue = "dev-bypass-user")
    String authBypassFirebaseUid;

    @ConfigProperty(name = "auth.dev-bypass.email", defaultValue = "dev-bypass@local")
    String authBypassEmail;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (HttpMethod.OPTIONS.equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        if (resourceInfo == null || resourceInfo.getResourceMethod() == null) {
            return;
        }

        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();

        // Check if endpoint is public
        if (isPublicEndpoint(method)) {
            tryOptionalAuthentication(requestContext);
            return;
        }

        // Get authentication requirements
        Authenticated methodAuth = method.getAnnotation(Authenticated.class);
        Authenticated classAuth = resourceClass.getAnnotation(Authenticated.class);
        Authenticated auth = methodAuth != null ? methodAuth : classAuth;

        // If no @Authenticated annotation, allow access (public by default)
        if (auth == null) {
            tryOptionalAuthentication(requestContext);
            return;
        }

        if (authBypassEnabled) {
            setBypassAuthentication();
            return;
        }

        // Authentication required
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || authHeader.isBlank()) {
            abortUnauthorized(requestContext, "Authorization header is required");
            return;
        }

        if (!tokenValidator.isReady()) {
            abortServiceUnavailable(requestContext,
                    "Authentication provider is not initialized. Check firebase.mock.enabled and FIREBASE_CREDENTIALS_PATH");
            return;
        }

        try {
            AuthenticatedUser authenticatedUser = validateAndBuildUser(authHeader);

            // Check roles if specified
            if (auth.roles().length > 0) {
                if (!checkRoles(authenticatedUser, auth.roles(), auth.requireAll())) {
                    abortForbidden(requestContext, "Insufficient permissions");
                    return;
                }
            }

            // Check if user is active
            if (authenticatedUser.isRegistered() && !authenticatedUser.isActive()) {
                abortForbidden(requestContext, "User account is deactivated");
                return;
            }

            // Set security context
            securityContext.setCurrentUser(authenticatedUser);
            securityContext.setRawToken(authHeader);
            securityContext.setAuthenticated(true);

            LOG.debugf("Authenticated user: %s (roles: %s)", 
                    authenticatedUser.getEmail(), 
                    authenticatedUser.getRoles());

        } catch (AuthenticationException e) {
            LOG.warn("Authentication failed: " + e.getMessage());
            abortUnauthorized(requestContext, e.getMessage());
        }
    }

    private void setBypassAuthentication() {
        AuthenticatedUser bypassUser = AuthenticatedUser.builder()
                .firebaseUid(authBypassFirebaseUid)
                .email(authBypassEmail)
                .name("Dev Bypass User")
                .emailVerified(true)
                .provider(AuthProvider.EMAIL)
                .roles(EnumSet.allOf(Role.class))
                .active(true)
                .build();

        securityContext.setCurrentUser(bypassUser);
        securityContext.setRawToken("DEV_BYPASS");
        securityContext.setAuthenticated(true);
    }

    private boolean isPublicEndpoint(Method method) {
        return method.isAnnotationPresent(PublicEndpoint.class);
    }

    /**
     * Validates Firebase token and builds authenticated user.
     */
    private AuthenticatedUser validateAndBuildUser(String authHeader) {
        DecodedToken decodedToken = tokenValidator.validateToken(authHeader);

        AuthenticatedUser.AuthenticatedUserBuilder builder = AuthenticatedUser.builder()
                .firebaseUid(decodedToken.getUid())
                .email(decodedToken.getEmail())
                .name(decodedToken.getName())
                .emailVerified(decodedToken.isEmailVerified())
                .provider(decodedToken.getProvider());

        return userProvider
                .enrichWithUserData(decodedToken.getUid(), builder)
                .build();
    }

    private void tryOptionalAuthentication(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && !authHeader.isBlank()) {
            try {
                AuthenticatedUser authenticatedUser = validateAndBuildUser(authHeader);

                securityContext.setCurrentUser(authenticatedUser);
                securityContext.setRawToken(authHeader);
                securityContext.setAuthenticated(true);
            } catch (AuthenticationException e) {
                // Token invalid on public endpoint - just continue without auth
                LOG.debug("Optional authentication failed: " + e.getMessage());
            }
        }
    }

    private boolean checkRoles(AuthenticatedUser user, String[] requiredRoles, boolean requireAll) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }

        List<Role> parsedRoles = Arrays.stream(requiredRoles)
                .map(this::parseRole)
                .toList();

        if (parsedRoles.stream().anyMatch(Objects::isNull)) {
            return false;
        }

        if (requireAll) {
            return parsedRoles.stream()
                    .allMatch(user::hasRole);
        } else {
            return parsedRoles.stream()
                    .anyMatch(user::hasRole);
        }
    }

    private Role parseRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            logInvalidRole(roleName);
            return null;
        }

        try {
            return Role.valueOf(roleName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logInvalidRole(roleName);
            return null;
        }
    }

    private void logInvalidRole(String roleName) {
        String resource = resourceInfo != null && resourceInfo.getResourceClass() != null
                ? resourceInfo.getResourceClass().getSimpleName()
                : "UnknownResource";
        String method = resourceInfo != null && resourceInfo.getResourceMethod() != null
                ? resourceInfo.getResourceMethod().getName()
                : "unknownMethod";

        LOG.errorf("Invalid @Authenticated role configuration '%s' at %s#%s. Access denied.", roleName, resource, method);
    }

    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ApiResponse.error(message))
                        .build()
        );
    }

    private void abortForbidden(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                        .entity(ApiResponse.error(message))
                        .build()
        );
    }

    private void abortServiceUnavailable(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(ApiResponse.error(message))
                        .build()
        );
    }
}
