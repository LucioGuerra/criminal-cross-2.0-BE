package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;

import java.util.List;

@ApplicationScoped
public class GetActivitySchedulesUseCase {

    @Inject
    ActivityScheduleRepository activityScheduleRepository;

    public List<ActivitySchedule> execute(Long headquartersId) {
        if (headquartersId == null) {
            return activityScheduleRepository.findAllActive();
        }
        return activityScheduleRepository.findByHeadquartersId(headquartersId);
    }
}
