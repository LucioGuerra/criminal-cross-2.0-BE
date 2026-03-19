package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class UpdateActivityScheduleUseCase {

    @Inject
    ActivityScheduleRepository activityScheduleRepository;

    public ActivitySchedule execute(Long id, ActivitySchedule updatedData) {
        if (id == null) {
            throw new BadRequestException("Schedule ID is required");
        }
        if (updatedData == null) {
            throw new BadRequestException("Schedule data is required");
        }

        ActivitySchedule existing = activityScheduleRepository.findById(id);
        if (existing == null) {
            throw new EntityNotFoundException("Activity schedule not found");
        }

        if (!hasAnyMutableField(updatedData)) {
            throw new BadRequestException("No updatable fields were provided");
        }

        if (updatedData.getDayOfWeek() != null) {
            existing.setDayOfWeek(updatedData.getDayOfWeek());
        }
        if (updatedData.getWeekDays() != null) {
            existing.setWeekDays(updatedData.getWeekDays());
        }
        if (updatedData.getStartTime() != null) {
            existing.setStartTime(updatedData.getStartTime());
        }
        if (updatedData.getDurationMinutes() != null) {
            existing.setDurationMinutes(updatedData.getDurationMinutes());
        }
        if (updatedData.getActivityId() != null) {
            existing.setActivityId(updatedData.getActivityId());
        }
        if (updatedData.getActive() != null) {
            existing.setActive(updatedData.getActive());
        }
        if (updatedData.getSchedulerType() != null) {
            existing.setSchedulerType(updatedData.getSchedulerType());
        }
        if (updatedData.getTemplateType() != null) {
            existing.setTemplateType(updatedData.getTemplateType());
        }
        if (updatedData.getActiveFrom() != null) {
            existing.setActiveFrom(updatedData.getActiveFrom());
        }
        if (updatedData.getActiveUntil() != null) {
            existing.setActiveUntil(updatedData.getActiveUntil());
        }
        if (updatedData.getScheduledDate() != null) {
            existing.setScheduledDate(updatedData.getScheduledDate());
        }

        existing.setId(id);
        return activityScheduleRepository.save(existing);
    }

    private boolean hasAnyMutableField(ActivitySchedule updatedData) {
        return updatedData.getDayOfWeek() != null
                || updatedData.getWeekDays() != null
                || updatedData.getStartTime() != null
                || updatedData.getDurationMinutes() != null
                || updatedData.getActivityId() != null
                || updatedData.getActive() != null
                || updatedData.getSchedulerType() != null
                || updatedData.getTemplateType() != null
                || updatedData.getActiveFrom() != null
                || updatedData.getActiveUntil() != null
                || updatedData.getScheduledDate() != null;
    }
}
