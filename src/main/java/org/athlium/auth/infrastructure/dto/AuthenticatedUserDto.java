package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for authenticated user response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUserDto {
    
    private String firebaseUid;
    private String email;
    private String name;
    private boolean emailVerified;
    private String provider;
    
    // Local user data
    private Long userId;
    private Set<String> roles;
    private boolean registered;
    private boolean active;
}
