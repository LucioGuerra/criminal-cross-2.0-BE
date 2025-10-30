package org.athlium.gym.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.athlium.gym.infrastructure.entity.ActivityEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ActivityPanacheRepository implements PanacheRepository<ActivityEntity> {
}
