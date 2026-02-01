package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.shared.exception.BadRequestException;

@ApplicationScoped
public class DeleteActivityUseCase {

    @Inject
    ActivityRepository activityRepository;

    @Transactional
    public void execute(Long id) {
        if (id == null) {
            throw new BadRequestException("Activity ID is required");
        }
        
        if (activityRepository.findById(id) == null) {
            throw new EntityNotFoundException("Activity", id);
        }
        
        activityRepository.delete(id);
    }
}