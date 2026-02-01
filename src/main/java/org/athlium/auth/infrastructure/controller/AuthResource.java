package org.athlium.auth.infrastructure.controller;

import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.application.service.AuthService;
import org.athlium.auth.application.usecase.LoginUseCase;
import org.athlium.auth.application.usecase.LogoutUseCase;
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

    /**
     * Verifies a Firebase ID token and returns the authenticated user info.
     * This is the main endpoint for validating tokens from the frontend.
     */
    @POST
    @Path("/verify-token")
    @PublicEndpoint
    public Response verifyToken(VerifyTokenRequestDto request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("ID token is required"))
                    .build();
        }

        try {
            AuthenticatedUser user = authService.verifyToken(request.getIdToken());

            AuthResponseDto response = AuthResponseDto.builder()
                    .user(authMapper.toDto(user))
                    .needsRegistration(!user.isRegistered())
                    .message(user.isRegistered() 
                            ? "Token verified successfully" 
                            : "Token verified - user needs registration")
                    .build();

            return Response.ok(ApiResponse.success(response)).build();

        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
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

    /**
     * Registers a new user in the local database.
     * Requires a valid Firebase ID token.
     */
    @POST
    @Path("/register")
    @PublicEndpoint
    public Response register(RegisterRequestDto request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("ID token is required"))
                    .build();
        }

        try {
            AuthenticatedUser user = authService.registerUser(
                    request.getIdToken(),
                    request.getName(),
                    request.getLastName()
            );

            AuthResponseDto response = AuthResponseDto.builder()
                    .user(authMapper.toDto(user))
                    .needsRegistration(false)
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

    /**
     * Login endpoint - validates Firebase token and returns refresh token.
     * This is the main entry point for authenticated sessions.
     */
    @POST
    @Path("/login")
    @PublicEndpoint
    public Response login(VerifyTokenRequestDto request, @Context HttpServerRequest httpRequest) {
        if (request == null || request.getIdToken() == null || request.getIdToken().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("ID token is required"))
                    .build();
        }

        try {
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);

            LoginUseCase.LoginResult result = authService.login(
                    request.getIdToken(),
                    deviceInfo,
                    ipAddress
            );

            if (result.needsRegistration()) {
                AuthResponseDto response = AuthResponseDto.builder()
                        .user(authMapper.toDto(result.user()))
                        .needsRegistration(true)
                        .message("User needs to complete registration")
                        .build();

                return Response.ok(ApiResponse.success(response)).build();
            }

            LoginResponseDto response = LoginResponseDto.builder()
                    .user(authMapper.toDto(result.user()))
                    .tokens(TokenPairDto.builder()
                            .accessToken(result.accessToken())
                            .refreshToken(result.refreshToken().getToken())
                            .expiresIn(900) // Custom JWT expires in 15 minutes (900 seconds)
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

    /**
     * Refresh token endpoint - exchanges a valid refresh token for new tokens.
     */
    @POST
    @Path("/refresh")
    @PublicEndpoint
    public Response refresh(RefreshTokenRequestDto request, @Context HttpServerRequest httpRequest) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Refresh token is required"))
                    .build();
        }

        try {
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);

            RefreshTokenUseCase.RefreshResult result = authService.refreshToken(
                    request.getRefreshToken(),
                    deviceInfo,
                    ipAddress
            );

            LoginResponseDto response = LoginResponseDto.builder()
                    .user(authMapper.toDto(result.user()))
                    .tokens(TokenPairDto.builder()
                            .accessToken(result.accessToken())
                            .refreshToken(result.newRefreshToken().getToken())
                            .expiresIn(900) // Custom JWT expires in 15 minutes
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

    /**
     * Logout endpoint - revokes refresh tokens.
     */
    @POST
    @Path("/logout")
    @PublicEndpoint
    public Response logout(LogoutRequestDto request) {
        try {
            LogoutUseCase.LogoutResult result;

            if (request != null && request.isLogoutAll()) {
                result = authService.logoutAll();
            } else if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
                result = authService.logout(request.getRefreshToken());
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Refresh token is required, or set logoutAll to true"))
                        .build();
            }

            return Response.ok(ApiResponse.success(result.message(), result)).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Logout failed: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Extracts the client IP address from the request, handling proxies.
     */
    private String getClientIpAddress(HttpServerRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.remoteAddress() != null ? request.remoteAddress().host() : "unknown";
    }
}