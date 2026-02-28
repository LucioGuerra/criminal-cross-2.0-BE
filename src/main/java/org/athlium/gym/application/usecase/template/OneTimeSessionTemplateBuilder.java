package org.athlium.gym.application.usecase.template;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SchedulerType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class OneTimeSessionTemplateBuilder implements SessionTemplateBuilder {

    @Override
    public boolean supports(ActivitySchedule schedule) {
        return schedule.getSchedulerType() == SchedulerType.ONE_TIME_DISPOSABLE;
    }

    @Override
    public List<SessionSlot> buildSlots(ActivitySchedule schedule, LocalDate weekStart) {
        if (schedule.getStartTime() == null || schedule.getDurationMinutes() == null) {
            return List.of();
        }
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate scheduledDate = schedule.getScheduledDate();
        if (scheduledDate == null) {
            return List.of();
        }
        if (scheduledDate.isBefore(weekStart) || scheduledDate.isAfter(weekEnd)) {
            return List.of();
        }

        var startsAt = LocalDateTime.of(scheduledDate, schedule.getStartTime()).toInstant(ZoneOffset.UTC);
        var endsAt = startsAt.plusSeconds(schedule.getDurationMinutes() * 60L);
        return List.of(new SessionSlot(startsAt, endsAt));
    }
}
