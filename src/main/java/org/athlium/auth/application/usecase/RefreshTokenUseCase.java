package org.athlium.auth.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.RefreshTokenStore;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.exception.AuthenticationException;
import org.athlium.auth.domain.exception.InvalidRefreshTokenException;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.RefreshToken;
import org.athlium.auth.infrastructure.security.JwtTokenGenerator;
import org.athlium.users.domain.model.User;
import org.jboss.logging.Logger;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use case for refreshing tokens.
 * Validates the refresh token and generates a new one (token rotation).
 */
@ApplicationScoped
public class RefreshTokenUseCase {

    private static final Logger LOG = Logger.getLogger(RefreshTokenUseCase.class);

    @Inject
    RefreshTokenStore refreshTokenStore;

    @Inject
    UserProvider userProvider;

    @Inject
    JwtTokenGenerator jwtTokenGenerator;

    /**
     * Result of a successful token refresh.
     */
    public record RefreshResult(
            AuthenticatedUser user,
            RefreshToken newRefreshToken,
            String accessToken
    ) {}

    /**
     * Refreshes tokens using a valid refresh token.
     * Implements token rotation: the old token is revoked and a new one is issued.
     *
     * @param refreshTokenValue The refresh token string
     * @param deviceInfo        Optional device information
     * @param ipAddress         Optional IP address
     * @return Refresh result with user info and new refresh token
     * @throws InvalidRefreshTokenException if token is invalid, expired, or revoked
     */
    public RefreshResult execute(String refreshTokenValue, String deviceInfo, String ipAddress) {
        LOG.debugf("Processing token refresh request");

        // Find the refresh token
        Optional<RefreshToken> tokenOpt = refreshTokenStore.findByToken(refreshTokenValue);

        if (tokenOpt.isEmpty()) {
            LOG.warnf("Refresh token not found");
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        RefreshToken existingToken = tokenOpt.get();

        // Validate the token
        if (existingToken.isRevoked()) {
            LOG.warnf("Attempted to use revoked refresh token for user %d", existingToken.getUserId());
            // Potential token theft - revoke all tokens for this user
            refreshTokenStore.revokeAllForUser(existingToken.getUserId());
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        if (existingToken.isExpired()) {
            LOG.warnf("Attempted to use expired refresh token for user %d", existingToken.getUserId());
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        // Find the user
        Optional<User> userOpt = userProvider.findByFirebaseUid(existingToken.getFirebaseUid());

        if (userOpt.isEmpty()) {
            LOG.warnf("User not found for refresh token: %s", existingToken.getFirebaseUid());
            throw new InvalidRefreshTokenException("User not found");
        }

        User user = userOpt.get();

        // Check if user is still active
        if (!Boolean.TRUE.equals(user.getActive())) {
            LOG.warnf("Inactive user attempted token refresh: %d", user.getId());
            refreshTokenStore.revokeAllForUser(user.getId());
            throw new AuthenticationException("User account is deactivated");
        }

        // Token rotation: revoke old token and create new one
        refreshTokenStore.revokeToken(refreshTokenValue);

        RefreshToken newToken = refreshTokenStore.createToken(
                existingToken.getFirebaseUid(),
                existingToken.getUserId(),
                deviceInfo != null ? deviceInfo : existingToken.getDeviceInfo(),
                ipAddress != null ? ipAddress : existingToken.getIpAddress()
        );

        // Build authenticated user
        AuthenticatedUser authenticatedUser = userProvider.enrichWithUserData(
                existingToken.getFirebaseUid(),
                AuthenticatedUser.builder()
                        .firebaseUid(user.getFirebaseUid())
                        .email(user.getEmail())
                        .name(user.getName())
                        .emailVerified(true) // Assume verified if they have a valid refresh token
                        .provider(null) // We don't store provider in refresh token
        ).build();

        // Generate new custom JWT access token
        String accessToken = jwtTokenGenerator.generateAccessToken(
                user.getFirebaseUid(),
                user.getEmail(),
                user.getId(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );

        LOG.infof("Token refreshed for user %d", user.getId());

        return new RefreshResult(authenticatedUser, newToken, accessToken);
    }
}
