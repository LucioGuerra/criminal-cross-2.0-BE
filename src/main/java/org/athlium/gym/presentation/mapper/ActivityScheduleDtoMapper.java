package org.athlium.gym.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SchedulerType;
import org.athlium.gym.domain.model.SessionTemplateType;
import org.athlium.gym.domain.model.WeekDay;
import org.athlium.gym.presentation.dto.ActivityScheduleRequest;
import org.athlium.gym.presentation.dto.ActivityScheduleResponse;
import org.athlium.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ActivityScheduleDtoMapper {

    public ActivitySchedule toDomain(ActivityScheduleRequest request) {
        if (request == null) {
            return null;
        }

        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(request.getOrganizationId());
        schedule.setHeadquartersId(request.getHeadquartersId());
        schedule.setActivityId(request.getActivityId());
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setWeekDays(parseWeekDays(request.getWeekDays(), request.getDayOfWeek()));
        schedule.setDurationMinutes(request.getDurationMinutes());
        schedule.setActive(request.getActive());
        schedule.setStartTime(parseStartTime(request.getStartTime()));
        schedule.setSchedulerType(parseSchedulerType(request.getSchedulerType(), request.getTemplateType()));
        schedule.setTemplateType(parseTemplateType(request.getTemplateType()));
        schedule.setActiveFrom(parseDate(request.getActiveFrom(), "activeFrom"));
        schedule.setActiveUntil(parseDate(request.getActiveUntil(), "activeUntil"));
        schedule.setScheduledDate(parseDate(request.getScheduledDate(), "scheduledDate"));
        return schedule;
    }

    public ActivityScheduleResponse toResponse(ActivitySchedule schedule) {
        ActivityScheduleResponse response = new ActivityScheduleResponse();
        response.setId(schedule.getId());
        response.setOrganizationId(schedule.getOrganizationId());
        response.setHeadquartersId(schedule.getHeadquartersId());
        response.setActivityId(schedule.getActivityId());
        response.setDayOfWeek(resolveLegacyDayOfWeek(schedule));
        response.setWeekDays(formatWeekDays(schedule.getWeekDays()));
        response.setStartTime(schedule.getStartTime() != null ? schedule.getStartTime().toString() : null);
        response.setDurationMinutes(schedule.getDurationMinutes());
        response.setActive(schedule.getActive());
        response.setSchedulerType(schedule.getSchedulerType() != null ? schedule.getSchedulerType().name() : null);
        response.setTemplateType(resolveTemplateType(schedule));
        response.setActiveFrom(formatDate(schedule.getActiveFrom()));
        response.setActiveUntil(formatDate(schedule.getActiveUntil()));
        response.setScheduledDate(formatDate(schedule.getScheduledDate()));
        return response;
    }

    public List<ActivityScheduleResponse> toResponseList(List<ActivitySchedule> schedules) {
        return schedules.stream().map(this::toResponse).toList();
    }

    private LocalTime parseStartTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("startTime must use HH:mm or HH:mm:ss format");
        }
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(fieldName + " must use YYYY-MM-DD format");
        }
    }

    private String formatDate(LocalDate value) {
        return value != null ? value.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    private SessionTemplateType parseTemplateType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return SessionTemplateType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("templateType must be WEEKLY_RANGE, ONE_TIME or ONE_TIME_DISPOSABLE");
        }
    }

    private SchedulerType parseSchedulerType(String schedulerType, String templateType) {
        if (schedulerType != null && !schedulerType.isBlank()) {
            try {
                return SchedulerType.valueOf(schedulerType.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("schedulerType must be WEEKLY_RANGE or ONE_TIME_DISPOSABLE");
            }
        }

        SessionTemplateType legacyType = parseTemplateType(templateType);
        if (legacyType == null || legacyType == SessionTemplateType.WEEKLY_RANGE) {
            return SchedulerType.WEEKLY_RANGE;
        }
        if (legacyType == SessionTemplateType.ONE_TIME || legacyType == SessionTemplateType.ONE_TIME_DISPOSABLE) {
            return SchedulerType.ONE_TIME_DISPOSABLE;
        }
        throw new BadRequestException("Unsupported templateType");
    }

    private List<WeekDay> parseWeekDays(List<String> weekDays, Integer dayOfWeek) {
        if (weekDays != null && !weekDays.isEmpty()) {
            return weekDays.stream().map(this::parseWeekDay).distinct().toList();
        }
        if (dayOfWeek == null) {
            return null;
        }
        try {
            return List.of(WeekDay.fromIsoDayValue(dayOfWeek));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("dayOfWeek must be between 1 and 7");
        }
    }

    private WeekDay parseWeekDay(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("weekDays cannot contain blank values");
        }
        try {
            return WeekDay.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("weekDays entries must be MONDAY to SUNDAY");
        }
    }

    private List<String> formatWeekDays(List<WeekDay> weekDays) {
        if (weekDays == null || weekDays.isEmpty()) {
            return null;
        }
        return weekDays.stream().map(Enum::name).toList();
    }

    private String resolveTemplateType(ActivitySchedule schedule) {
        if (schedule.getTemplateType() != null) {
            return schedule.getTemplateType().name();
        }
        if (schedule.getSchedulerType() == SchedulerType.ONE_TIME_DISPOSABLE) {
            return SessionTemplateType.ONE_TIME_DISPOSABLE.name();
        }
        if (schedule.getSchedulerType() == SchedulerType.WEEKLY_RANGE) {
            return SessionTemplateType.WEEKLY_RANGE.name();
        }
        return null;
    }

    private Integer resolveLegacyDayOfWeek(ActivitySchedule schedule) {
        if (schedule.getDayOfWeek() != null) {
            return schedule.getDayOfWeek();
        }
        if (schedule.getWeekDays() != null && !schedule.getWeekDays().isEmpty()) {
            return schedule.getWeekDays().get(0).toIsoDayValue();
        }
        return null;
    }
}
