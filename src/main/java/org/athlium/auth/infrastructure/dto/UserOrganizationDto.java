package org.athlium.auth.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationDto {

    private Long id;
    private String name;
    private List<UserHeadquartersDto> headquarters;
}
