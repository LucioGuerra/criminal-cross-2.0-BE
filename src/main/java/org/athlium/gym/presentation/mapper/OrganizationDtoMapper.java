package org.athlium.gym.presentation.mapper;

import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.presentation.dto.OrganizationInput;
import org.athlium.gym.presentation.dto.OrganizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface OrganizationDtoMapper {

    Organization toDomain(OrganizationInput input);

    @Mapping(target = "headquarters", ignore = true)
    OrganizationResponse toResponse(Organization domain);
}