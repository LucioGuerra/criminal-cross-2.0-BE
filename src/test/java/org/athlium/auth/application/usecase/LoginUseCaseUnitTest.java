package org.athlium.auth.application.usecase;

import org.athlium.auth.application.ports.FirebaseIdentityProvider;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.DecodedToken;
import org.athlium.auth.domain.model.FirebaseSessionTokens;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginUseCaseUnitTest {

    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase();
        useCase.firebaseIdentityProvider = new StubFirebaseIdentityProvider();
        useCase.tokenValidator = new StubTokenValidator();
        useCase.userProvider = new StubUserProvider();
    }

    @Test
    void shouldReturnFirebaseTokensAndEnrichedLocalRoles() {
        LoginUseCase.LoginResult result = useCase.execute("ana@example.com", "secret-123");

        assertEquals("firebase-access-token", result.tokens().accessToken());
        assertEquals("firebase-refresh-token", result.tokens().refreshToken());
        assertEquals(3600L, result.tokens().expiresInSeconds());

        assertEquals("firebase-uid-100", result.user().getFirebaseUid());
        assertEquals(15L, result.user().getUserId());
        assertTrue(result.user().hasRole(Role.CLIENT));
        assertTrue(result.user().isRegistered());
    }

    private static class StubFirebaseIdentityProvider implements FirebaseIdentityProvider {
        @Override
        public FirebaseSessionTokens register(String email, String password, String displayName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FirebaseSessionTokens login(String email, String password) {
            return new FirebaseSessionTokens("firebase-access-token", "firebase-refresh-token", 3600L, "firebase-uid-100", email);
        }

        @Override
        public FirebaseSessionTokens refresh(String refreshToken) {
            throw new UnsupportedOperationException();
        }
    }

    private static class StubTokenValidator implements TokenValidator {
        @Override
        public DecodedToken validateToken(String idToken) {
            return DecodedToken.builder()
                    .uid("firebase-uid-100")
                    .email("ana@example.com")
                    .name("Ana")
                    .emailVerified(true)
                    .provider(AuthProvider.EMAIL)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        }

        @Override
        public boolean isReady() {
            return true;
        }
    }

    private static class StubUserProvider implements UserProvider {
        @Override
        public Optional<User> findByFirebaseUid(String firebaseUid) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public User syncUser(String firebaseUid, String email, String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AuthenticatedUser.AuthenticatedUserBuilder enrichWithUserData(String firebaseUid, AuthenticatedUser.AuthenticatedUserBuilder builder) {
            return builder
                    .userId(15L)
                    .roles(EnumSet.of(Role.CLIENT))
                    .active(true);
        }

        @Override
        public User createUser(String firebaseUid, String email, String name, String lastName) {
            throw new UnsupportedOperationException();
        }
    }
}
