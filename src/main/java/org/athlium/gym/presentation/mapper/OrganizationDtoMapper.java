package org.athlium.gym.presentation.mapper;

import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.presentation.dto.OrganizationInput;
import org.athlium.gym.presentation.dto.OrganizationHeadquartersResponse;
import org.athlium.gym.presentation.dto.OrganizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "jakarta-cdi")
public interface OrganizationDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "headQuarters", ignore = true)
    Organization toDomain(OrganizationInput input);

    @Mapping(source = "headQuarters", target = "headquarters")
    OrganizationResponse toResponse(Organization domain);

    OrganizationHeadquartersResponse toHeadquartersResponse(Headquarters domain);
}
