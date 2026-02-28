package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SchedulerType;
import org.athlium.gym.domain.model.SessionTemplateType;
import org.athlium.gym.domain.model.WeekDay;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class CreateActivityScheduleUseCase {

    @Inject
    ActivityScheduleRepository activityScheduleRepository;

    public ActivitySchedule execute(ActivitySchedule schedule) {
        if (schedule == null) {
            throw new BadRequestException("Schedule body is required");
        }
        if (schedule.getOrganizationId() == null || schedule.getOrganizationId() <= 0) {
            throw new BadRequestException("organizationId must be a positive number");
        }
        if (schedule.getHeadquartersId() == null || schedule.getHeadquartersId() <= 0) {
            throw new BadRequestException("headquartersId must be a positive number");
        }
        if (schedule.getActivityId() == null || schedule.getActivityId() <= 0) {
            throw new BadRequestException("activityId must be a positive number");
        }
        if (schedule.getStartTime() == null) {
            throw new BadRequestException("startTime is required");
        }
        if (schedule.getDurationMinutes() == null || schedule.getDurationMinutes() <= 0) {
            throw new BadRequestException("durationMinutes must be greater than 0");
        }
        normalizeSchedulerType(schedule);
        normalizeWeekDays(schedule);

        validateTemplateFields(schedule);

        if (schedule.getActive() == null) {
            schedule.setActive(true);
        }
        return activityScheduleRepository.save(schedule);
    }

    private void validateTemplateFields(ActivitySchedule schedule) {
        if (schedule.getSchedulerType() == SchedulerType.WEEKLY_RANGE) {
            if (schedule.getWeekDays() == null || schedule.getWeekDays().isEmpty()) {
                throw new BadRequestException("weekDays must contain at least one weekday for WEEKLY_RANGE schedulers");
            }
            if ((schedule.getActiveFrom() == null) != (schedule.getActiveUntil() == null)) {
                throw new BadRequestException("activeFrom and activeUntil must be provided together for WEEKLY_RANGE templates");
            }
            if (schedule.getActiveFrom() != null && schedule.getActiveFrom().isAfter(schedule.getActiveUntil())) {
                throw new BadRequestException("activeFrom must be less than or equal to activeUntil");
            }
            if (schedule.getScheduledDate() != null) {
                throw new BadRequestException("scheduledDate is not allowed for WEEKLY_RANGE templates");
            }
            schedule.setTemplateType(SessionTemplateType.WEEKLY_RANGE);
            schedule.setDayOfWeek(schedule.getWeekDays().get(0).toIsoDayValue());
            return;
        }

        if (schedule.getSchedulerType() == SchedulerType.ONE_TIME_DISPOSABLE) {
            if (schedule.getScheduledDate() == null) {
                throw new BadRequestException("scheduledDate is required for ONE_TIME_DISPOSABLE schedulers");
            }
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            if (schedule.getScheduledDate().isBefore(today)) {
                throw new BadRequestException("scheduledDate must be today or in the future");
            }
            if (schedule.getActiveFrom() != null || schedule.getActiveUntil() != null) {
                throw new BadRequestException("activeFrom/activeUntil are not allowed for ONE_TIME_DISPOSABLE schedulers");
            }
            if (schedule.getWeekDays() != null && !schedule.getWeekDays().isEmpty()) {
                throw new BadRequestException("weekDays are not allowed for ONE_TIME_DISPOSABLE schedulers");
            }
            schedule.setSchedulerType(SchedulerType.ONE_TIME_DISPOSABLE);
            schedule.setTemplateType(SessionTemplateType.ONE_TIME_DISPOSABLE);
            schedule.setDayOfWeek(schedule.getScheduledDate().getDayOfWeek().getValue());
            schedule.setWeekDays(List.of(WeekDay.fromIsoDayValue(schedule.getDayOfWeek())));
            return;
        }

        throw new BadRequestException("Unsupported scheduler type");
    }

    private void normalizeSchedulerType(ActivitySchedule schedule) {
        if (schedule.getSchedulerType() != null) {
            return;
        }

        SessionTemplateType templateType = schedule.getTemplateType();
        if (templateType == null || templateType == SessionTemplateType.WEEKLY_RANGE) {
            schedule.setSchedulerType(SchedulerType.WEEKLY_RANGE);
            return;
        }

        if (templateType == SessionTemplateType.ONE_TIME || templateType == SessionTemplateType.ONE_TIME_DISPOSABLE) {
            schedule.setSchedulerType(SchedulerType.ONE_TIME_DISPOSABLE);
            return;
        }

        throw new BadRequestException("Unsupported template type");
    }

    private void normalizeWeekDays(ActivitySchedule schedule) {
        if (schedule.getSchedulerType() == SchedulerType.ONE_TIME_DISPOSABLE) {
            schedule.setWeekDays(null);
            return;
        }

        if (schedule.getWeekDays() != null && !schedule.getWeekDays().isEmpty()) {
            schedule.setDayOfWeek(schedule.getWeekDays().get(0).toIsoDayValue());
            return;
        }

        if (schedule.getDayOfWeek() != null) {
            try {
                WeekDay weekDay = WeekDay.fromIsoDayValue(schedule.getDayOfWeek());
                schedule.setWeekDays(List.of(weekDay));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("dayOfWeek must be between 1 and 7");
            }
        }
    }
}
