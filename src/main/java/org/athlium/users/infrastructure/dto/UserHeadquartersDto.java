package org.athlium.users.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserHeadquartersDto {
    private Long id;
    private String name;
    private UserOrganizationDto organization;
}
