package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GetActivitySchedulesUseCase {

    @Inject
    ActivityScheduleRepository activityScheduleRepository;

    @Inject
    ActivityRepository activityRepository;

    public List<ActivitySchedule> execute(Long headquartersId) {
        List<ActivitySchedule> schedules;
        if (headquartersId == null) {
            schedules = activityScheduleRepository.findAllActive();
        } else {
            schedules = activityScheduleRepository.findByHeadquartersId(headquartersId);
        }

        enrichActivities(schedules);
        return schedules;
    }

    private void enrichActivities(List<ActivitySchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        List<Long> activityIds = schedules.stream()
                .map(ActivitySchedule::getActivityId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        Map<Long, Activity> activitiesById = activityRepository.findByIds(activityIds);
        schedules.forEach(schedule -> schedule.setActivity(activitiesById.get(schedule.getActivityId())));
    }
}
