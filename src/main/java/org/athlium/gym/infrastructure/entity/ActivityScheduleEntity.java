package org.athlium.gym.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(
        name = "activity_schedules",
        indexes = {
                @Index(name = "idx_activity_schedules_hq_day", columnList = "headquarters_id, day_of_week"),
                @Index(name = "idx_activity_schedules_activity_day", columnList = "activity_id, day_of_week")
        }
)
public class ActivityScheduleEntity extends PanacheEntity {

    @Column(name = "organization_id", nullable = false)
    public Long organizationId;

    @Column(name = "headquarters_id", nullable = false)
    public Long headquartersId;

    @Column(name = "activity_id", nullable = false)
    public Long activityId;

    @Column(name = "day_of_week", nullable = false)
    public Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    public LocalTime startTime;

    @Column(name = "duration_minutes", nullable = false)
    public Integer durationMinutes;

    @Column(name = "is_active", nullable = false)
    public Boolean active = true;
}
