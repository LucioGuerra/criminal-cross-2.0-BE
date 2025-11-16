package org.athlium.users.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateUserRequestDto {
    private String firebaseUid;
    private String email;
    private String name;
    private String lastName;
}