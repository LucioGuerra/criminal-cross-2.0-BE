package org.athlium.users.infrastructure.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.athlium.users.domain.model.Role;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UpdateRolesRequestDto {
    @NotEmpty(message = "roles cannot be empty")
    private Set<@NotNull(message = "role cannot be null") Role> roles;
}
