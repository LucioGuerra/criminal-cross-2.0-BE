package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class DeleteActivityScheduleUseCase {

    @Inject
    ActivityScheduleRepository activityScheduleRepository;

    public ActivitySchedule execute(Long id) {
        if (id == null) {
            throw new BadRequestException("Schedule ID is required");
        }

        ActivitySchedule schedule = activityScheduleRepository.findById(id);
        if (schedule == null) {
            throw new EntityNotFoundException("Activity schedule not found");
        }

        schedule.setActive(false);
        return activityScheduleRepository.save(schedule);
    }
}
