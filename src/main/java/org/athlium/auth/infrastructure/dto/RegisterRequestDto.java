package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration.
 * The frontend sends the Firebase ID token along with additional profile information.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    /**
     * The Firebase ID token obtained from client-side authentication.
     */
    private String idToken;

    /**
     * User's first name.
     */
    private String name;

    /**
     * User's last name.
     */
    private String lastName;
}
