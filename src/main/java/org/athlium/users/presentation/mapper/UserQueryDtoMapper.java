package org.athlium.users.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import org.athlium.users.domain.model.UserHqMembership;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.infrastructure.dto.UserHeadquartersDto;
import org.athlium.users.infrastructure.dto.UserOrganizationDto;
import org.athlium.users.infrastructure.dto.UserResponseDto;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UserQueryDtoMapper {

    public UserResponseDto toResponse(UserWithPackageStatus domain) {
        if (domain == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(domain.getId())
                .name(domain.getName())
                .lastName(domain.getLastName())
                .email(domain.getEmail())
                .roles(domain.getRoles())
                .headquarters(toHeadquartersDtos(domain.getHqMemberships()))
                .active(domain.getActive())
                .packageStatus(domain.getPackageStatus() != null ? domain.getPackageStatus().name() : null)
                .periodEnd(domain.getPeriodEnd())
                .daysRemaining(domain.getDaysRemaining())
                .build();
    }

    public List<UserResponseDto> toResponseList(List<UserWithPackageStatus> domains) {
        if (domains == null) {
            return null;
        }

        List<UserResponseDto> responses = new ArrayList<>();
        for (UserWithPackageStatus domain : domains) {
            responses.add(toResponse(domain));
        }
        return responses;
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
