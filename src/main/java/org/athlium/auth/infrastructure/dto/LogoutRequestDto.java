package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for logout operations.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequestDto {

    /**
     * The refresh token to revoke.
     * If not provided, all tokens for the user will be revoked.
     */
    private String refreshToken;

    /**
     * If true, revokes all refresh tokens for the user (logout from all devices).
     */
    private boolean logoutAll;
}
