package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.repository.ActivityRepository;

@ApplicationScoped
public class GetActivityUseCase {

    @Inject
    ActivityRepository activityRepository;

    public Activity execute(Long id) {
        return activityRepository.findById(id);
    }
}