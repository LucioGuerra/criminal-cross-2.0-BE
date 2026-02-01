package org.athlium.auth.infrastructure.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.exception.AuthenticationException;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.DecodedToken;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.users.domain.model.Role;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * JAX-RS filter that intercepts requests and validates tokens.
 * Supports both custom JWT tokens (issued by backend) and Firebase tokens.
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
    CustomJwtValidator customJwtValidator;

    @Inject
    UserProvider userProvider;

    @Inject
    SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
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

        // Authentication required
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || authHeader.isBlank()) {
            abortUnauthorized(requestContext, "Authorization header is required");
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

    private boolean isPublicEndpoint(Method method) {
        return method.isAnnotationPresent(PublicEndpoint.class);
    }

    /**
     * Validates token and builds authenticated user.
     * Supports both custom JWT (backend-issued) and Firebase tokens.
     */
    private AuthenticatedUser validateAndBuildUser(String authHeader) {
        // Check if it's a custom JWT or Firebase token
        if (customJwtValidator.isCustomJwt(authHeader)) {
            // Validate custom JWT
            CustomJwtValidator.JwtValidationResult jwtResult = customJwtValidator.validateCustomJwt(authHeader);
            
            // Build authenticated user from JWT claims
            // For custom JWTs, we trust the embedded data (userId, roles)
            AuthenticatedUser.AuthenticatedUserBuilder builder = AuthenticatedUser.builder()
                    .firebaseUid(jwtResult.firebaseUid())
                    .email(jwtResult.email())
                    .emailVerified(true); // Custom JWTs are only issued after Firebase verification
            
            // Enrich with fresh user data from database
            return userProvider
                    .enrichWithUserData(jwtResult.firebaseUid(), builder)
                    .build();
        } else {
            // Validate Firebase token (original flow)
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

        if (requireAll) {
            return Arrays.stream(requiredRoles)
                    .map(Role::valueOf)
                    .allMatch(user::hasRole);
        } else {
            return Arrays.stream(requiredRoles)
                    .map(Role::valueOf)
                    .anyMatch(user::hasRole);
        }
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
}
