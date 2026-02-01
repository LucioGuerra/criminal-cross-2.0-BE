package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response containing user data and tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    
    /**
     * The authenticated user information.
     */
    private AuthenticatedUserDto user;
    
    /**
     * Whether the user needs to complete registration.
     * True if user authenticated but doesn't exist in local database.
     */
    private boolean needsRegistration;
    
    /**
     * Message about the authentication status.
     */
    private String message;
}
