package org.athlium.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Domain model representing a refresh token.
 * Refresh tokens are long-lived tokens stored in the database
 * that can be used to obtain new access tokens.
 */
@Getter
@Builder
public class RefreshToken {

    private Long id;
    private String token;
    private String firebaseUid;
    private Long userId;
    private String deviceInfo;
    private String ipAddress;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean revoked;
    private Instant revokedAt;

    /**
     * Checks if the token is expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks if the token is valid (not expired and not revoked).
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }

    /**
     * Revokes this token.
     */
    public RefreshToken revoke() {
        return RefreshToken.builder()
                .id(this.id)
                .token(this.token)
                .firebaseUid(this.firebaseUid)
                .userId(this.userId)
                .deviceInfo(this.deviceInfo)
                .ipAddress(this.ipAddress)
                .createdAt(this.createdAt)
                .expiresAt(this.expiresAt)
                .revoked(true)
                .revokedAt(Instant.now())
                .build();
    }
}
