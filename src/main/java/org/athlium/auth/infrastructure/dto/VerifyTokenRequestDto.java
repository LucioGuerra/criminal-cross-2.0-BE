package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for token verification request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTokenRequestDto {
    
    /**
     * The Firebase ID token to verify.
     */
    private String idToken;
}
