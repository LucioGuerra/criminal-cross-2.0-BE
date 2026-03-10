package org.athlium.users.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import org.athlium.users.domain.model.UserWithPackageStatus;
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
                .headquartersIds(domain.getHeadquartersIds())
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
}
