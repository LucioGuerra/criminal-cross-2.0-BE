package org.athlium.auth.infrastructure.mapper;

import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.dto.AuthenticatedUserDto;
import org.athlium.users.domain.model.Role;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between auth domain models and DTOs.
 */
@ApplicationScoped
public class AuthMapper {

    /**
     * Converts an AuthenticatedUser domain model to DTO.
     */
    public AuthenticatedUserDto toDto(AuthenticatedUser user) {
        if (user == null) {
            return null;
        }

        return AuthenticatedUserDto.builder()
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .name(user.getName())
                .emailVerified(user.isEmailVerified())
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .userId(user.getUserId())
                .roles(rolesToStrings(user.getRoles()))
                .registered(user.isRegistered())
                .active(user.isActive())
                .build();
    }

    private Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::name)
                .collect(Collectors.toSet());
    }
}
