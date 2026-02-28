package org.athlium.gym.application.usecase.template;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SchedulerType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class WeeklyRangeSessionTemplateBuilder implements SessionTemplateBuilder {

    @Override
    public boolean supports(ActivitySchedule schedule) {
        return schedule.getSchedulerType() == null || schedule.getSchedulerType() == SchedulerType.WEEKLY_RANGE;
    }

    @Override
    public List<SessionSlot> buildSlots(ActivitySchedule schedule, LocalDate weekStart) {
        if (schedule.getWeekDays() == null || schedule.getWeekDays().isEmpty() || schedule.getStartTime() == null || schedule.getDurationMinutes() == null) {
            return List.of();
        }

        List<SessionSlot> slots = new ArrayList<>();
        for (var weekDay : schedule.getWeekDays()) {
            LocalDate sessionDate = weekStart.plusDays(weekDay.toIsoDayValue() - 1L);
            if (schedule.getActiveFrom() != null && sessionDate.isBefore(schedule.getActiveFrom())) {
                continue;
            }
            if (schedule.getActiveUntil() != null && sessionDate.isAfter(schedule.getActiveUntil())) {
                continue;
            }

            var startsAt = LocalDateTime.of(sessionDate, schedule.getStartTime()).toInstant(ZoneOffset.UTC);
            var endsAt = startsAt.plusSeconds(schedule.getDurationMinutes() * 60L);
            slots.add(new SessionSlot(startsAt, endsAt));
        }
        return slots;
    }
}
