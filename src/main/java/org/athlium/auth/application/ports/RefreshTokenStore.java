package org.athlium.auth.application.ports;

import org.athlium.auth.domain.model.RefreshToken;

import java.util.Optional;

/**
 * Port interface for refresh token operations.
 */
public interface RefreshTokenStore {

    /**
     * Creates and persists a new refresh token.
     *
     * @param firebaseUid User's Firebase UID
     * @param userId      User's local database ID
     * @param deviceInfo  Optional device information
     * @param ipAddress   Optional IP address
     * @return The created refresh token
     */
    RefreshToken createToken(String firebaseUid, Long userId, String deviceInfo, String ipAddress);

    /**
     * Finds a refresh token by its token string.
     *
     * @param token The token string
     * @return Optional containing the token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Revokes a specific refresh token.
     *
     * @param token The token string to revoke
     * @return true if revoked, false if not found
     */
    boolean revokeToken(String token);

    /**
     * Revokes all refresh tokens for a user.
     *
     * @param userId User's local database ID
     * @return Number of tokens revoked
     */
    long revokeAllForUser(Long userId);

    /**
     * Revokes all refresh tokens for a Firebase UID.
     *
     * @param firebaseUid User's Firebase UID
     * @return Number of tokens revoked
     */
    long revokeAllForFirebaseUid(String firebaseUid);

    /**
     * Counts active sessions for a user.
     *
     * @param userId User's local database ID
     * @return Number of active sessions
     */
    long countActiveSessions(Long userId);

    /**
     * Cleans up expired tokens.
     *
     * @return Number of tokens deleted
     */
    long cleanupExpiredTokens();
}
