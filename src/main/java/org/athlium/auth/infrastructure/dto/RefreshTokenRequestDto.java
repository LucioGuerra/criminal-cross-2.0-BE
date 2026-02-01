package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for refreshing tokens.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto {

    /**
     * The refresh token to use for obtaining a new access token.
     */
    private String refreshToken;
}
