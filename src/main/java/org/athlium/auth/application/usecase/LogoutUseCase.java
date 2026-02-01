package org.athlium.auth.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.RefreshTokenStore;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.security.SecurityContext;
import org.jboss.logging.Logger;

/**
 * Use case for user logout.
 * Revokes refresh tokens to end user sessions.
 */
@ApplicationScoped
public class LogoutUseCase {

    private static final Logger LOG = Logger.getLogger(LogoutUseCase.class);

    @Inject
    RefreshTokenStore refreshTokenStore;

    @Inject
    SecurityContext securityContext;

    /**
     * Result of a logout operation.
     */
    public record LogoutResult(
            long tokensRevoked,
            String message
    ) {}

    /**
     * Logs out by revoking a specific refresh token.
     *
     * @param refreshToken The refresh token to revoke
     * @return Logout result
     */
    public LogoutResult execute(String refreshToken) {
        LOG.debugf("Processing logout request for specific token");

        boolean revoked = refreshTokenStore.revokeToken(refreshToken);

        if (revoked) {
            LOG.info("Refresh token revoked successfully");
            return new LogoutResult(1, "Logged out successfully");
        } else {
            LOG.warn("Refresh token not found for logout");
            return new LogoutResult(0, "Token not found or already revoked");
        }
    }

    /**
     * Logs out from all devices by revoking all refresh tokens for the current user.
     *
     * @return Logout result with count of revoked tokens
     */
    public LogoutResult executeLogoutAll() {
        if (!securityContext.isAuthenticated()) {
            LOG.warn("Logout all attempted without authentication");
            return new LogoutResult(0, "Not authenticated");
        }

        AuthenticatedUser currentUser = securityContext.getCurrentUser();

        if (currentUser.getUserId() != null) {
            long count = refreshTokenStore.revokeAllForUser(currentUser.getUserId());
            LOG.infof("Logged out from all devices: %d tokens revoked for user %d", 
                    count, currentUser.getUserId());
            return new LogoutResult(count, "Logged out from all devices");
        } else {
            long count = refreshTokenStore.revokeAllForFirebaseUid(currentUser.getFirebaseUid());
            LOG.infof("Logged out from all devices: %d tokens revoked for Firebase UID %s", 
                    count, currentUser.getFirebaseUid());
            return new LogoutResult(count, "Logged out from all devices");
        }
    }

    /**
     * Logs out a specific user from all devices (admin operation).
     *
     * @param userId The user ID to logout
     * @return Logout result
     */
    public LogoutResult executeForUser(Long userId) {
        long count = refreshTokenStore.revokeAllForUser(userId);
        LOG.infof("Admin logout: %d tokens revoked for user %d", count, userId);
        return new LogoutResult(count, "User logged out from all devices");
    }
}
