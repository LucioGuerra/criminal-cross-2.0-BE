package org.athlium.gym.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.presentation.dto.ActivityScheduleRequest;
import org.athlium.gym.presentation.dto.ActivityScheduleResponse;
import org.athlium.shared.exception.BadRequestException;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

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
        schedule.setDurationMinutes(request.getDurationMinutes());
        schedule.setActive(request.getActive());
        schedule.setStartTime(parseStartTime(request.getStartTime()));
        return schedule;
    }

    public ActivityScheduleResponse toResponse(ActivitySchedule schedule) {
        ActivityScheduleResponse response = new ActivityScheduleResponse();
        response.setId(schedule.getId());
        response.setOrganizationId(schedule.getOrganizationId());
        response.setHeadquartersId(schedule.getHeadquartersId());
        response.setActivityId(schedule.getActivityId());
        response.setDayOfWeek(schedule.getDayOfWeek());
        response.setStartTime(schedule.getStartTime() != null ? schedule.getStartTime().toString() : null);
        response.setDurationMinutes(schedule.getDurationMinutes());
        response.setActive(schedule.getActive());
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
}
