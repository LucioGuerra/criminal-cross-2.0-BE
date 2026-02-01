package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO for login and token refresh operations.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    /**
     * The authenticated user information.
     */
    private AuthenticatedUserDto user;

    /**
     * The token pair (access + refresh).
     */
    private TokenPairDto tokens;

    /**
     * Response message.
     */
    private String message;
}
