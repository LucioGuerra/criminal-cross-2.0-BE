package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.repository.ActivityRepository;

@ApplicationScoped
public class CreateActivityUseCase {

    @Inject
    ActivityRepository activityRepository;

    @Transactional
    public Activity execute(String name, String description, String tenantId) {
        Activity activity = Activity.createNew(name, description, tenantId);
        return activityRepository.save(activity);
    }
}