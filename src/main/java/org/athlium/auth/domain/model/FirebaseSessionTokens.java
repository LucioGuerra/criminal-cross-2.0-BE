package org.athlium.auth.domain.model;

public record FirebaseSessionTokens(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String firebaseUid,
        String email
) {
}
