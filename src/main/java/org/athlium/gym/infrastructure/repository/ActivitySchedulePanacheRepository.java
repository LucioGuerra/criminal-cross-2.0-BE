package org.athlium.gym.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.infrastructure.entity.ActivityScheduleEntity;

@ApplicationScoped
public class ActivitySchedulePanacheRepository implements PanacheRepository<ActivityScheduleEntity> {
}
