package org.athlium.gym.infrastructure.mapper;

import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.infrastructure.entity.ActivityEntity;
import org.mapstruct.*;

@Mapper(componentModel = "jakarta-cdi")
public interface ActivityMapper {

    Activity toDomain(ActivityEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "headquarters", ignore = true)
    ActivityEntity toEntity(Activity domain);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "headquarters", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDomain(Activity domain, @MappingTarget ActivityEntity entity);
}
