package org.athlium.gym.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.infrastructure.entity.SessionInstanceEntity;

@ApplicationScoped
public class SessionInstancePanacheRepository implements PanacheRepository<SessionInstanceEntity> {
}
