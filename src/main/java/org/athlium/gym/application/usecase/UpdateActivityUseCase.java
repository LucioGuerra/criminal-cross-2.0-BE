package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.shared.exception.BadRequestException;

@ApplicationScoped
public class UpdateActivityUseCase {

    @Inject
    ActivityRepository activityRepository;

    @Transactional
    public Activity execute(Activity activity) {
        if (activity == null) {
            throw new BadRequestException("Activity is required");
        }
        if (activity.getId() == null) {
            throw new BadRequestException("Activity ID is required for update");
        }
        
        Activity existing = activityRepository.findById(activity.getId());
        if (existing == null) {
            throw new EntityNotFoundException("Activity", activity.getId());
        }
        
        return activityRepository.update(activity);
    }
}