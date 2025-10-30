package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.repository.ActivityRepository;

@ApplicationScoped
public class DeleteActivityUseCase {

    @Inject
    ActivityRepository activityRepository;

    public void execute(Long id) {
        activityRepository.delete(id);
    }
}