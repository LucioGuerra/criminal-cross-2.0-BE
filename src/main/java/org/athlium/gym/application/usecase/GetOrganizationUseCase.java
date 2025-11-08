package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.domain.repository.OrganizationRepository;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class GetOrganizationUseCase {

    @Inject
    OrganizationRepository organizationRepository;

    public Organization execute(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + id));
    }
}