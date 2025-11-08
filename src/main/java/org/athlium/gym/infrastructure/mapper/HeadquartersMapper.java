package org.athlium.gym.infrastructure.mapper;

import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.infrastructure.entity.HeadquartersEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi", uses = ActivityMapper.class)
public interface HeadquartersMapper {

    Headquarters toDomain(HeadquartersEntity entity);

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "activities", ignore = true)
    HeadquartersEntity toEntity(Headquarters domain);
}