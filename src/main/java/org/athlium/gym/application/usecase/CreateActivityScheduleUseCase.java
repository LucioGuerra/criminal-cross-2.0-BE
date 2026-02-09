package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.shared.exception.BadRequestException;

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
        if (schedule.getDayOfWeek() == null || schedule.getDayOfWeek() < 1 || schedule.getDayOfWeek() > 7) {
            throw new BadRequestException("dayOfWeek must be between 1 and 7");
        }
        if (schedule.getStartTime() == null) {
            throw new BadRequestException("startTime is required");
        }
        if (schedule.getDurationMinutes() == null || schedule.getDurationMinutes() <= 0) {
            throw new BadRequestException("durationMinutes must be greater than 0");
        }
        if (schedule.getActive() == null) {
            schedule.setActive(true);
        }
        return activityScheduleRepository.save(schedule);
    }
}
