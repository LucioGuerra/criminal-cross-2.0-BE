package org.athlium.users.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.athlium.users.domain.model.Role;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UpdateRolesRequestDto {
    private Set<Role> roles;
}