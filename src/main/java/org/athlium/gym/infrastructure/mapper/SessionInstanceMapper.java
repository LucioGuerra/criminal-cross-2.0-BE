package org.athlium.gym.infrastructure.mapper;

import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.infrastructure.entity.SessionInstanceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "jakarta-cdi")
public interface SessionInstanceMapper {

    @Mapping(target = "activity", ignore = true)
    @Mapping(target = "participants", ignore = true)
    SessionInstance toDomain(SessionInstanceEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SessionInstanceEntity toEntity(SessionInstance domain);
}
