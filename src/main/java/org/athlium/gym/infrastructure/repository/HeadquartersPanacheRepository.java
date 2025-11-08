package org.athlium.gym.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.infrastructure.entity.HeadquartersEntity;

import java.util.List;

@ApplicationScoped
public class HeadquartersPanacheRepository implements PanacheRepository<HeadquartersEntity> {
    
    public List<HeadquartersEntity> findByOrganizationId(Long organizationId) {
        return list("organizationId", organizationId);
    }
}