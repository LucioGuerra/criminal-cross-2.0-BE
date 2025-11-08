package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.repository.OrganizationRepository;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class DeleteOrganizationUseCase {

    @Inject
    OrganizationRepository organizationRepository;

    @Transactional
    public void execute(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new EntityNotFoundException("Organization not found with id: " + id);
        }
        organizationRepository.deleteById(id);
    }
}