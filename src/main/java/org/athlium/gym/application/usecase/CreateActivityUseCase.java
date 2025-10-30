package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.shared.exception.BadRequestException;

@ApplicationScoped
public class CreateActivityUseCase {

    @Inject
    ActivityRepository activityRepository;

    @Transactional
    public Activity execute(String name, String description, String tenantId) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Activity name is required");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new BadRequestException("Tenant ID is required");
        }
        
        Activity activity = Activity.createNew(name, description, tenantId);
        return activityRepository.save(activity);
    }
}