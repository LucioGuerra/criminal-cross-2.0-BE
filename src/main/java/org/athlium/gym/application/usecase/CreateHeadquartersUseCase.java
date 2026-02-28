package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.domain.repository.HeadquartersRepository;

@ApplicationScoped
public class CreateHeadquartersUseCase {

    @Inject
    HeadquartersRepository headquartersRepository;

    @Inject
    GetOrganizationUseCase getOrganizationUseCase;

    @Transactional
    public Headquarters execute(Headquarters headquarters) {
        Organization organization = getOrganizationUseCase.execute(headquarters.getOrganizationId());
        return headquartersRepository.save(headquarters);
    }
}
