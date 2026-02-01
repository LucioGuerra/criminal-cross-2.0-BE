package org.athlium.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Value object representing decoded token claims from Firebase.
 * This is used internally before enriching with local user data.
 */
@Getter
@Builder
public class DecodedToken {

    private final String uid;
    private final String email;
    private final String name;
    private final String picture;
    private final boolean emailVerified;
    private final AuthProvider provider;
    private final Instant issuedAt;
    private final Instant expiresAt;

    /**
     * Checks if the token has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
