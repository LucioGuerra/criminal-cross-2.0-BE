package org.athlium.gym.domain.repository;

import org.athlium.gym.domain.model.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository {
    Organization save(Organization organization);
    Optional<Organization> findById(Long id);
    List<Organization> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
}