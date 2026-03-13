package org.athlium.gym.presentation.mapper;

import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.presentation.dto.HeadquartersInput;
import org.athlium.gym.presentation.dto.HeadquartersOrganizationResponse;
import org.athlium.gym.presentation.dto.HeadquartersResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "jakarta-cdi", uses = ActivityDtoMapper.class)
public interface HeadquartersDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activities", ignore = true)
    Headquarters toDomain(HeadquartersInput input);

    @Mapping(target = "organization", ignore = true)
    HeadquartersResponse toResponse(Headquarters domain);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    HeadquartersOrganizationResponse toOrganizationResponse(Organization organization);
}
