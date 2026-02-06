package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.repository.SessionConfigurationRepository;
import org.athlium.shared.exception.BadRequestException;

@ApplicationScoped
public class UpsertHeadquartersConfigUseCase {

    @Inject
    SessionConfigurationRepository sessionConfigurationRepository;

    public SessionConfiguration execute(Long headquartersId, SessionConfiguration configuration) {
        validateId(headquartersId, "Headquarters");
        SessionConfigurationValidator.validate(configuration);
        return sessionConfigurationRepository.upsertHeadquartersConfig(headquartersId, configuration);
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BadRequestException(name + " ID must be a positive number");
        }
    }
}
