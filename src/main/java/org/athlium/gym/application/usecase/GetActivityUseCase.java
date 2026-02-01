package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.shared.exception.BadRequestException;

@ApplicationScoped
public class GetActivityUseCase {

    @Inject
    ActivityRepository activityRepository;

    public Activity execute(Long id) {
        if (id == null) {
            throw new BadRequestException("Activity ID is required");
        }
        
        Activity activity = activityRepository.findById(id);
        if (activity == null) {
            throw new EntityNotFoundException("Activity", id);
        }
        
        return activity;
    }
}