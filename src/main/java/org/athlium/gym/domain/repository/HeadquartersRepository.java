package org.athlium.gym.domain.repository;

import org.athlium.gym.domain.model.Headquarters;

import java.util.List;
import java.util.Optional;

public interface HeadquartersRepository {
    Headquarters save(Headquarters headquarters);
    Optional<Headquarters> findById(Long id);
    List<Headquarters> findAll();
    List<Headquarters> findByOrganizationId(Long organizationId);
    void deleteById(Long id);
    boolean existsById(Long id);
}