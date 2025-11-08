package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.domain.repository.OrganizationRepository;

@ApplicationScoped
public class CreateOrganizationUseCase {

    @Inject
    OrganizationRepository organizationRepository;

    @Transactional
    public Organization execute(Organization organization) {
        return organizationRepository.save(organization);
    }
}