package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration.
 * The frontend sends user profile plus credentials.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    private String email;

    private String password;

    /**
     * User's first name.
     */
    private String name;

    /**
     * User's last name.
     */
    private String lastName;
}
