package org.athlium.gym.infrastructure.mapper;

import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.infrastructure.entity.SessionInstanceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jakarta-cdi")
public interface SessionInstanceMapper {

    SessionInstance toDomain(SessionInstanceEntity entity);
}
