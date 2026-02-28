package org.athlium.gym.domain.repository;

import org.athlium.gym.domain.model.ActivitySchedule;

import java.util.List;

public interface ActivityScheduleRepository {

    ActivitySchedule save(ActivitySchedule schedule);

    List<ActivitySchedule> findAllActive();

    List<ActivitySchedule> findByHeadquartersId(Long headquartersId);
}
