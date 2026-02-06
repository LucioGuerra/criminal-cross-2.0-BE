package org.athlium.users.infrastructure.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateUserRequestDto {
    @NotBlank(message = "firebaseUid is required")
    private String firebaseUid;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "lastName is required")
    private String lastName;
}
