package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for auth module health check response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthHealthDto {
    
    private boolean firebaseInitialized;
    private boolean mockModeEnabled;
    private String status;
}
