package org.athlium.auth.infrastructure.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.auth.application.service.AuthService;
import org.athlium.auth.application.usecase.LoginUseCase;
import org.athlium.auth.application.usecase.RefreshTokenUseCase;
import org.athlium.auth.application.usecase.RegisterUserUseCase;
import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.FirebaseSessionTokens;
import org.athlium.auth.infrastructure.dto.LoginRequestDto;
import org.athlium.auth.infrastructure.dto.RegisterRequestDto;
import org.athlium.auth.infrastructure.dto.VerifyTokenRequestDto;
import org.athlium.auth.infrastructure.mapper.AuthMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.users.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthResourceUnitTest {

    private AuthResource resource;

    @BeforeEach
    void setUp() {
        resource = new AuthResource();
        resource.authMapper = new AuthMapper();
        resource.authService = new StubAuthService();
    }

    @Test
    void shouldReturnGoneForDeprecatedVerifyTokenEndpoint() {
        Response response = resource.verifyToken(new VerifyTokenRequestDto("legacy-token"));

        assertEquals(410, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.getMessage().contains("deprecated"));
    }

    @Test
    void shouldValidateLoginPayload() {
        Response response = resource.login(new LoginRequestDto("ana@example.com", ""));

        assertEquals(400, response.getStatus());
    }

    @Test
    void shouldReturnFirebaseTokensOnRegister() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .email("ana@example.com")
                .password("secret-123")
                .name("Ana")
                .lastName("Lopez")
                .build();

        Response response = resource.register(request);

        assertEquals(201, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
    }

    @Test
    void shouldReturnCurrentUserOnMeEndpoint() {
        Response response = resource.getCurrentUser();

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
    }

    private static class StubAuthService extends AuthService {
        private AuthenticatedUser authUser() {
            return AuthenticatedUser.builder()
                    .firebaseUid("firebase-uid-100")
                    .email("ana@example.com")
                    .name("Ana")
                    .emailVerified(true)
                    .provider(AuthProvider.EMAIL)
                    .userId(99L)
                    .roles(EnumSet.of(Role.CLIENT))
                    .active(true)
                    .build();
        }

        @Override
        public RegisterUserUseCase.RegisterResult registerUser(String email, String password, String name, String lastName) {
            return new RegisterUserUseCase.RegisterResult(
                    authUser(),
                    new FirebaseSessionTokens("firebase-access", "firebase-refresh", 3600L, "firebase-uid-100", email)
            );
        }

        @Override
        public LoginUseCase.LoginResult login(String email, String password) {
            return new LoginUseCase.LoginResult(
                    authUser(),
                    new FirebaseSessionTokens("firebase-access", "firebase-refresh", 3600L, "firebase-uid-100", email)
            );
        }

        @Override
        public RefreshTokenUseCase.RefreshResult refreshToken(String refreshToken) {
            return new RefreshTokenUseCase.RefreshResult(
                    authUser(),
                    new FirebaseSessionTokens("firebase-access", "firebase-refresh", 3600L, "firebase-uid-100", "ana@example.com")
            );
        }

        @Override
        public AuthenticatedUser getCurrentUser() {
            return authUser();
        }
    }
}
