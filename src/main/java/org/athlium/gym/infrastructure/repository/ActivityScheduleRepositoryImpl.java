package org.athlium.gym.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.gym.infrastructure.mapper.ActivityScheduleMapper;

import java.util.List;

@ApplicationScoped
public class ActivityScheduleRepositoryImpl implements ActivityScheduleRepository {

    @Inject
    ActivitySchedulePanacheRepository panacheRepository;

    @Inject
    ActivityScheduleMapper mapper;

    @Override
    public ActivitySchedule save(ActivitySchedule schedule) {
        var entity = mapper.toEntity(schedule);
        if (schedule.getId() != null) {
            entity.id = schedule.getId();
        }
        panacheRepository.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public List<ActivitySchedule> findAllActive() {
        return panacheRepository.find("active", true).list().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ActivitySchedule> findByHeadquartersId(Long headquartersId) {
        return panacheRepository.find("headquartersId", headquartersId).list().stream().map(mapper::toDomain).toList();
    }
}
