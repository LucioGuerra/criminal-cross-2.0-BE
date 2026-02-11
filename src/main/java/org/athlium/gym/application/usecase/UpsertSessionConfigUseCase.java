package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.repository.SessionConfigurationRepository;
import org.athlium.shared.exception.BadRequestException;

@ApplicationScoped
public class UpsertSessionConfigUseCase {

    @Inject
    SessionConfigurationRepository sessionConfigurationRepository;

    public SessionConfiguration execute(Long sessionId, SessionConfiguration configuration) {
        validateId(sessionId, "Session");
        SessionConfigurationValidator.validate(configuration);
        return sessionConfigurationRepository.upsertSessionConfig(sessionId, configuration);
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BadRequestException(name + " ID must be a positive number");
        }
    }
}
