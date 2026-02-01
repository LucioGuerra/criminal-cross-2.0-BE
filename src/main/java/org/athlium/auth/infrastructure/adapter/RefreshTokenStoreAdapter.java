package org.athlium.auth.infrastructure.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.auth.application.ports.RefreshTokenStore;
import org.athlium.auth.domain.model.RefreshToken;
import org.athlium.auth.infrastructure.entity.RefreshTokenEntity;
import org.athlium.auth.infrastructure.mapper.RefreshTokenMapper;
import org.athlium.auth.infrastructure.repository.RefreshTokenRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

/**
 * Implementation of RefreshTokenStore using PostgreSQL.
 */
@ApplicationScoped
public class RefreshTokenStoreAdapter implements RefreshTokenStore {

    private static final Logger LOG = Logger.getLogger(RefreshTokenStoreAdapter.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 64; // 64 bytes = 512 bits

    @Inject
    RefreshTokenRepository repository;

    @Inject
    RefreshTokenMapper mapper;

    @ConfigProperty(name = "auth.refresh-token.expiration-days", defaultValue = "30")
    int expirationDays;

    @Override
    @Transactional
    public RefreshToken createToken(String firebaseUid, Long userId, String deviceInfo, String ipAddress) {
        String tokenValue = generateSecureToken();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationDays, ChronoUnit.DAYS);

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .token(tokenValue)
                .firebaseUid(firebaseUid)
                .userId(userId)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .createdAt(now)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        repository.persist(entity);
        LOG.infof("Created refresh token for user %d, expires at %s", userId, expiresAt);

        return mapper.toDomain(entity);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public boolean revokeToken(String token) {
        Optional<RefreshTokenEntity> entityOpt = repository.findByToken(token);
        if (entityOpt.isPresent()) {
            RefreshTokenEntity entity = entityOpt.get();
            entity.setRevoked(true);
            entity.setRevokedAt(Instant.now());
            LOG.infof("Revoked refresh token for user %d", entity.getUserId());
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public long revokeAllForUser(Long userId) {
        long count = repository.revokeAllByUserId(userId);
        LOG.infof("Revoked %d refresh tokens for user %d", count, userId);
        return count;
    }

    @Override
    @Transactional
    public long revokeAllForFirebaseUid(String firebaseUid) {
        long count = repository.revokeAllByFirebaseUid(firebaseUid);
        LOG.infof("Revoked %d refresh tokens for Firebase UID %s", count, firebaseUid);
        return count;
    }

    @Override
    public long countActiveSessions(Long userId) {
        return repository.countActiveByUserId(userId);
    }

    @Override
    @Transactional
    public long cleanupExpiredTokens() {
        long count = repository.deleteExpiredTokens();
        if (count > 0) {
            LOG.infof("Cleaned up %d expired refresh tokens", count);
        }
        return count;
    }

    /**
     * Generates a cryptographically secure random token.
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
