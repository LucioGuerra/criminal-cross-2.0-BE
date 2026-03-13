package org.athlium.users.infrastructure.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.athlium.users.domain.model.UserHqMembership;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserQueryRepository;
import org.athlium.users.infrastructure.dto.UserHeadquartersDto;
import org.athlium.users.infrastructure.dto.UserOrganizationDto;
import org.athlium.users.infrastructure.dto.UserResponseDto;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UserDtoMapper {

    @Inject
    UserQueryRepository userQueryRepository;

    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        List<UserHqMembership> memberships = user.getId() != null
                ? userQueryRepository.findHqMembershipsByUserIds(List.of(user.getId()))
                : List.of();

        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .firebaseUid(user.getFirebaseUid())
                .roles(user.getRoles())
                .headquarters(toHeadquartersDtos(memberships))
                .active(user.getActive())
                .build();
    }

    private List<UserHeadquartersDto> toHeadquartersDtos(List<UserHqMembership> memberships) {
        if (memberships == null || memberships.isEmpty()) {
            return List.of();
        }

        List<UserHeadquartersDto> headquarters = new ArrayList<>();
        for (UserHqMembership membership : memberships) {
            headquarters.add(UserHeadquartersDto.builder()
                    .id(membership.getHqId())
                    .name(membership.getHqName())
                    .organization(UserOrganizationDto.builder()
                            .id(membership.getOrganizationId())
                            .name(membership.getOrganizationName())
                            .build())
                    .build());
        }
        return headquarters;
    }
}
