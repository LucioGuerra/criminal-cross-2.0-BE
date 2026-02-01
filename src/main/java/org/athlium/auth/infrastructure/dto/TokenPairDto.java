package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing both access and refresh tokens.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPairDto {

    /**
     * The Firebase ID token (access token).
     * Short-lived, used for API authentication.
     */
    private String accessToken;

    /**
     * The refresh token.
     * Long-lived, used to obtain new access tokens.
     */
    private String refreshToken;

    /**
     * Access token expiration in seconds from now.
     */
    private long expiresIn;

    /**
     * Token type (always "Bearer").
     */
    @Builder.Default
    private String tokenType = "Bearer";
}
