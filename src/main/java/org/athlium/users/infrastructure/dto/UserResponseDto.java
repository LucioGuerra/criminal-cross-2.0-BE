package org.athlium.users.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.athlium.users.domain.model.Role;

import java.util.Set;

@AllArgsConstructor
@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String firebaseUid;
    private Set<Role> roles;
    private Boolean active;
}