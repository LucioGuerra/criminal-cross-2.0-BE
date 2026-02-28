package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.repository.SessionConfigurationRepository;
import org.athlium.shared.exception.BadRequestException;

@ApplicationScoped
public class ResolveSessionConfigurationUseCase {

    @Inject
    SessionConfigurationRepository sessionConfigurationRepository;

    public SessionConfiguration execute(Long organizationId, Long headquartersId, Long activityId, Long sessionId) {
        validateRequiredIds(organizationId, headquartersId, activityId);

        SessionConfiguration effective = SessionConfiguration.defaults();

        effective = effective.mergeWith(sessionConfigurationRepository.getOrganizationConfig(organizationId).orElse(null));
        effective = effective.mergeWith(sessionConfigurationRepository.getHeadquartersConfig(headquartersId).orElse(null));
        effective = effective.mergeWith(sessionConfigurationRepository.getActivityConfig(activityId).orElse(null));

        if (sessionId != null) {
            effective = effective.mergeWith(sessionConfigurationRepository.getSessionConfig(sessionId).orElse(null));
        }

        return effective;
    }

    private void validateRequiredIds(Long organizationId, Long headquartersId, Long activityId) {
        if (organizationId == null || organizationId <= 0) {
            throw new BadRequestException("organizationId is required and must be positive");
        }
        if (headquartersId == null || headquartersId <= 0) {
            throw new BadRequestException("headquartersId is required and must be positive");
        }
        if (activityId == null || activityId <= 0) {
            throw new BadRequestException("activityId is required and must be positive");
        }
    }
}
