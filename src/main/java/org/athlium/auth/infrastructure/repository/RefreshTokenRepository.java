package org.athlium.auth.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import org.athlium.auth.infrastructure.entity.RefreshTokenEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for refresh token persistence operations.
 */
@ApplicationScoped
public class RefreshTokenRepository implements PanacheRepository<RefreshTokenEntity> {

    /**
     * Finds a refresh token by its token string.
     */
    public Optional<RefreshTokenEntity> findByToken(String token) {
        return find("token", token).firstResultOptional();
    }

    /**
     * Finds all active (non-revoked, non-expired) tokens for a user.
     */
    public List<RefreshTokenEntity> findActiveByUserId(Long userId) {
        return list("userId = ?1 and revoked = false and expiresAt > ?2", userId, Instant.now());
    }

    /**
     * Finds all active tokens for a Firebase UID.
     */
    public List<RefreshTokenEntity> findActiveByFirebaseUid(String firebaseUid) {
        return list("firebaseUid = ?1 and revoked = false and expiresAt > ?2", firebaseUid, Instant.now());
    }

    /**
     * Revokes all tokens for a user (used on logout from all devices).
     */
    public long revokeAllByUserId(Long userId) {
        return update("revoked = true, revokedAt = ?1 where userId = ?2 and revoked = false",
                Instant.now(), userId);
    }

    /**
     * Revokes all tokens for a Firebase UID.
     */
    public long revokeAllByFirebaseUid(String firebaseUid) {
        return update("revoked = true, revokedAt = ?1 where firebaseUid = ?2 and revoked = false",
                Instant.now(), firebaseUid);
    }

    /**
     * Deletes expired tokens (cleanup job).
     */
    public long deleteExpiredTokens() {
        return delete("expiresAt < ?1", Instant.now());
    }

    /**
     * Counts active sessions for a user.
     */
    public long countActiveByUserId(Long userId) {
        return count("userId = ?1 and revoked = false and expiresAt > ?2", userId, Instant.now());
    }
}
