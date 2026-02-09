package org.athlium.gym.infrastructure.mapper;

import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.infrastructure.entity.ActivityScheduleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jakarta-cdi")
public interface ActivityScheduleMapper {

    ActivitySchedule toDomain(ActivityScheduleEntity entity);

    ActivityScheduleEntity toEntity(ActivitySchedule schedule);
}
