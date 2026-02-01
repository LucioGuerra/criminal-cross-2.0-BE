package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.domain.repository.OrganizationRepository;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class UpdateOrganizationUseCase {

    @Inject
    OrganizationRepository organizationRepository;

    @Transactional
    public Organization execute(Long id, Organization organization) {
        if (!organizationRepository.existsById(id)) {
            throw new EntityNotFoundException("Organization not found with id: " + id);
        }
        organization.setId(id);
        return organizationRepository.save(organization);
    }
}