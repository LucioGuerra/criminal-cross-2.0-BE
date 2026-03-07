package org.athlium.auth.infrastructure.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.application.service.AuthService;
import org.athlium.auth.application.usecase.LoginUseCase;
import org.athlium.auth.application.usecase.RefreshTokenUseCase;
import org.athlium.auth.domain.exception.AuthenticationException;
import org.athlium.auth.domain.exception.InvalidRefreshTokenException;
import org.athlium.auth.domain.exception.UserAlreadyExistsException;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.config.FirebaseConfig;
import org.athlium.auth.infrastructure.dto.*;
import org.athlium.auth.infrastructure.mapper.AuthMapper;
import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.auth.infrastructure.security.PublicEndpoint;
import org.athlium.shared.dto.ApiResponse;

/**
 * REST controller for authentication endpoints.
 */
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    AuthMapper authMapper;

    @Inject
    FirebaseConfig firebaseConfig;

    /**
     * Health check endpoint for the auth module.
     */
    @GET
    @Path("/health")
    @PublicEndpoint
    public Response health() {
        AuthHealthDto health = AuthHealthDto.builder()
                .firebaseInitialized(firebaseConfig.isInitialized())
                .mockModeEnabled(firebaseConfig.isMockEnabled())
                .status(authService.isReady() ? "ready" : "not_ready")
                .build();

        return Response.ok(ApiResponse.success(health)).build();
    }

    @POST
    @Path("/verify-token")
    @PublicEndpoint
    public Response verifyToken(VerifyTokenRequestDto request) {
        return Response.status(Response.Status.GONE)
                .entity(ApiResponse.error("Endpoint deprecated. Use /api/auth/me with Authorization: Bearer <firebase-id-token>"))
                .build();
    }

    /**
     * Returns the current authenticated user's information.
     * Requires a valid token in the Authorization header.
     */
    @GET
    @Path("/me")
    @Authenticated
    public Response getCurrentUser() {
        try {
            AuthenticatedUser user = authService.getCurrentUser();

            AuthResponseDto response = AuthResponseDto.builder()
                    .user(authMapper.toDto(user))
                    .needsRegistration(!user.isRegistered())
                    .message("Current user retrieved successfully")
                    .build();

            return Response.ok(ApiResponse.success(response)).build();

        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/register")
    @PublicEndpoint
    public Response register(RegisterRequestDto request) {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Email and password are required"))
                    .build();
        }

        try {
            var result = authService.registerUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getName(),
                    request.getLastName()
            );

            LoginResponseDto response = LoginResponseDto.builder()
                    .user(authMapper.toDto(result.user()))
                    .tokens(TokenPairDto.builder()
                            .accessToken(result.tokens().accessToken())
                            .refreshToken(result.tokens().refreshToken())
                            .expiresIn(result.tokens().expiresInSeconds())
                            .build())
                    .message("User registered successfully")
                    .build();

            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("User registered successfully", response))
                    .build();

        } catch (UserAlreadyExistsException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/login")
    @PublicEndpoint
    public Response login(LoginRequestDto request) {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Email and password are required"))
                    .build();
        }

        try {
            LoginUseCase.LoginResult result = authService.login(
                    request.getEmail(),
                    request.getPassword()
            );

            LoginResponseDto response = LoginResponseDto.builder()
                    .user(authMapper.toDto(result.user()))
                    .tokens(TokenPairDto.builder()
                            .accessToken(result.tokens().accessToken())
                            .refreshToken(result.tokens().refreshToken())
                            .expiresIn(result.tokens().expiresInSeconds())
                            .build())
                    .message("Login successful")
                    .build();

            return Response.ok(ApiResponse.success(response)).build();

        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    @PublicEndpoint
    public Response refresh(RefreshTokenRequestDto request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Refresh token is required"))
                    .build();
        }

        try {
            RefreshTokenUseCase.RefreshResult result = authService.refreshToken(
                    request.getRefreshToken()
            );

            LoginResponseDto response = LoginResponseDto.builder()
                    .user(authMapper.toDto(result.user()))
                    .tokens(TokenPairDto.builder()
                            .accessToken(result.tokens().accessToken())
                            .refreshToken(result.tokens().refreshToken())
                            .expiresIn(result.tokens().expiresInSeconds())
                            .build())
                    .message("Token refreshed successfully")
                    .build();

            return Response.ok(ApiResponse.success(response)).build();

        } catch (InvalidRefreshTokenException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
