package org.athlium.gym.infrastructure.mapper;

import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.infrastructure.document.ActivityScheduleDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "jakarta-cdi")
public interface ActivityScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateType", ignore = true)
    ActivitySchedule toDomain(ActivityScheduleDocument document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "scheduleId", ignore = true)
    ActivityScheduleDocument toDocument(ActivitySchedule schedule);
}
