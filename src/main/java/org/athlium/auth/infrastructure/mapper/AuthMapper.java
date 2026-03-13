package org.athlium.auth.infrastructure.mapper;

import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.dto.AuthenticatedUserDto;
import org.athlium.auth.infrastructure.dto.UserHeadquartersDto;
import org.athlium.auth.infrastructure.dto.UserOrganizationDto;
import org.athlium.users.domain.model.Role;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
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
                .organization(toOrganizationDto(user))
                .registered(user.isRegistered())
                .active(user.isActive())
                .build();
    }

    private UserOrganizationDto toOrganizationDto(AuthenticatedUser user) {
        if (user.getOrganizationId() == null) {
            return null;
        }

        return UserOrganizationDto.builder()
                .id(user.getOrganizationId())
                .name(user.getOrganizationName())
                .headquarters(toHeadquartersDtos(user.getHeadquarters()))
                .build();
    }

    private List<UserHeadquartersDto> toHeadquartersDtos(List<AuthenticatedUser.AuthenticatedHeadquarters> headquarters) {
        if (headquarters == null || headquarters.isEmpty()) {
            return List.of();
        }

        return headquarters.stream()
                .map(headquarter -> UserHeadquartersDto.builder()
                        .id(headquarter.getId())
                        .name(headquarter.getName())
                        .build())
                .toList();
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
