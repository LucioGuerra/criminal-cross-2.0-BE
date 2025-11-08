package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.domain.repository.OrganizationRepository;

import java.util.List;

@ApplicationScoped
public class GetOrganizationsUseCase {

    @Inject
    OrganizationRepository organizationRepository;

    public List<Organization> execute() {
        return organizationRepository.findAll();
    }
}