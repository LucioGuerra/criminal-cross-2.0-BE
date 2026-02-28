package org.athlium.gym.infrastructure.document;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.athlium.gym.domain.model.SchedulerType;
import org.athlium.gym.domain.model.WeekDay;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@MongoEntity(collection = "activity_schedulers")
public class ActivityScheduleDocument extends PanacheMongoEntity {

    public Long scheduleId;
    public Long organizationId;
    public Long headquartersId;
    public Long activityId;
    public Integer dayOfWeek;
    public List<WeekDay> weekDays;
    public LocalTime startTime;
    public Integer durationMinutes;
    public Boolean active = true;
    public SchedulerType schedulerType = SchedulerType.WEEKLY_RANGE;
    public LocalDate activeFrom;
    public LocalDate activeUntil;
    public LocalDate scheduledDate;
}
