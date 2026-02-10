package org.athlium.gym.presentation.mapper;

import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.presentation.dto.HeadquartersInput;
import org.athlium.gym.presentation.dto.HeadquartersResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "jakarta-cdi")
public interface HeadquartersDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activities", ignore = true)
    Headquarters toDomain(HeadquartersInput input);

    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "organization", ignore = true)
    HeadquartersResponse toResponse(Headquarters domain);
}
