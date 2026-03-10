package org.athlium.auth.infrastructure.security;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.exception.InvalidTokenException;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.DecodedToken;
import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FirebaseAuthFilterUnitTest {

    @Test
    void shouldReturn503WhenAuthProviderIsNotReady() throws IOException, NoSuchMethodException {
        FirebaseAuthFilter filter = new FirebaseAuthFilter();
        filter.resourceInfo = securedResourceInfo();
        filter.tokenValidator = new NotReadyTokenValidator();
        filter.userProvider = new PassthroughUserProvider();
        filter.securityContext = new SecurityContext();
        filter.authBypassEnabled = false;

        ResponseHolder responseHolder = new ResponseHolder();
        ContainerRequestContext requestContext = requestContext(HttpMethod.GET, "Bearer any-token", responseHolder);

        filter.filter(requestContext);

        assertNotNull(responseHolder.response);
        assertEquals(503, responseHolder.response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) responseHolder.response.getEntity();
        assertEquals(false, body.isSuccess());
    }

    @Test
    void shouldTreatInheritedAuthenticatedMethodAsProtected() throws IOException, NoSuchMethodException {
        FirebaseAuthFilter filter = new FirebaseAuthFilter();
        filter.resourceInfo = inheritedSecuredResourceInfo();
        filter.tokenValidator = new AlwaysValidTokenValidator();
        filter.userProvider = new PassthroughUserProvider();
        filter.securityContext = new SecurityContext();
        filter.authBypassEnabled = false;

        ResponseHolder responseHolder = new ResponseHolder();
        ContainerRequestContext requestContext = requestContext(HttpMethod.GET, null, responseHolder);

        filter.filter(requestContext);

        assertNotNull(responseHolder.response);
        assertEquals(401, responseHolder.response.getStatus());
    }

    @Test
    void shouldReturnUnauthorizedWhenInheritedAuthenticatedMethodHasInvalidToken() throws IOException, NoSuchMethodException {
        FirebaseAuthFilter filter = new FirebaseAuthFilter();
        filter.resourceInfo = inheritedSecuredResourceInfo();
        filter.tokenValidator = new InvalidTokenValidator();
        filter.userProvider = new PassthroughUserProvider();
        filter.securityContext = new SecurityContext();
        filter.authBypassEnabled = false;

        ResponseHolder responseHolder = new ResponseHolder();
        ContainerRequestContext requestContext = requestContext(HttpMethod.GET, "Bearer invalid-token", responseHolder);

        filter.filter(requestContext);

        assertNotNull(responseHolder.response);
        assertEquals(401, responseHolder.response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) responseHolder.response.getEntity();
        assertEquals("Invalid token", body.getMessage());
    }

    private ResourceInfo securedResourceInfo() throws NoSuchMethodException {
        Method method = SecuredEndpoint.class.getMethod("me");
        return (ResourceInfo) Proxy.newProxyInstance(
                ResourceInfo.class.getClassLoader(),
                new Class[]{ResourceInfo.class},
                (proxy, invokedMethod, args) -> {
                    if ("getResourceMethod".equals(invokedMethod.getName())) {
                        return method;
                    }
                    if ("getResourceClass".equals(invokedMethod.getName())) {
                        return SecuredEndpoint.class;
                    }
                    return null;
                }
        );
    }

    private ResourceInfo inheritedSecuredResourceInfo() throws NoSuchMethodException {
        Method method = SecuredEndpointProxy.class.getMethod("me");
        return (ResourceInfo) Proxy.newProxyInstance(
                ResourceInfo.class.getClassLoader(),
                new Class[]{ResourceInfo.class},
                (proxy, invokedMethod, args) -> {
                    if ("getResourceMethod".equals(invokedMethod.getName())) {
                        return method;
                    }
                    if ("getResourceClass".equals(invokedMethod.getName())) {
                        return SecuredEndpointProxy.class;
                    }
                    return null;
                }
        );
    }

    private ContainerRequestContext requestContext(String method, String authorizationHeader, ResponseHolder responseHolder) {
        InvocationHandler handler = (proxy, invokedMethod, args) -> {
            if ("getMethod".equals(invokedMethod.getName())) {
                return method;
            }
            if ("getHeaderString".equals(invokedMethod.getName())) {
                if (args != null && args.length == 1 && HttpHeaders.AUTHORIZATION.equals(args[0])) {
                    return authorizationHeader;
                }
                return null;
            }
            if ("abortWith".equals(invokedMethod.getName())) {
                responseHolder.response = (Response) args[0];
                return null;
            }
            return null;
        };

        return (ContainerRequestContext) Proxy.newProxyInstance(
                ContainerRequestContext.class.getClassLoader(),
                new Class[]{ContainerRequestContext.class},
                handler
        );
    }

    private static class ResponseHolder {
        Response response;
    }

    private static class NotReadyTokenValidator implements TokenValidator {
        @Override
        public DecodedToken validateToken(String idToken) {
            throw new InvalidTokenException("Firebase is not initialized");
        }

        @Override
        public boolean isReady() {
            return false;
        }
    }

    private static class InvalidTokenValidator implements TokenValidator {
        @Override
        public DecodedToken validateToken(String idToken) {
            throw new InvalidTokenException("Invalid token");
        }

        @Override
        public boolean isReady() {
            return true;
        }
    }

    private static class AlwaysValidTokenValidator implements TokenValidator {
        @Override
        public DecodedToken validateToken(String idToken) {
            return DecodedToken.builder()
                    .uid("uid-100")
                    .email("valid@test.com")
                    .name("Valid User")
                    .provider(AuthProvider.EMAIL)
                    .emailVerified(true)
                    .build();
        }

        @Override
        public boolean isReady() {
            return true;
        }
    }

    private static class PassthroughUserProvider implements UserProvider {
        @Override
        public Optional<org.athlium.users.domain.model.User> findByFirebaseUid(String firebaseUid) {
            return Optional.empty();
        }

        @Override
        public Optional<org.athlium.users.domain.model.User> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public org.athlium.users.domain.model.User syncUser(String firebaseUid, String email, String name) {
            return null;
        }

        @Override
        public AuthenticatedUser.AuthenticatedUserBuilder enrichWithUserData(String firebaseUid, AuthenticatedUser.AuthenticatedUserBuilder builder) {
            return builder;
        }

        @Override
        public org.athlium.users.domain.model.User createUser(String firebaseUid, String email, String name, String lastName) {
            return null;
        }
    }

    private static class SecuredEndpoint {
        @Authenticated
        public void me() {
        }
    }

    private static class SecuredEndpointProxy extends SecuredEndpoint {
        @Override
        public void me() {
            super.me();
        }
    }
}
