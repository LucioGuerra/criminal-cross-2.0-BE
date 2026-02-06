package org.athlium.gym.application.usecase;

import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.model.WaitlistStrategy;
import org.athlium.shared.exception.BadRequestException;

public final class SessionConfigurationValidator {

    private SessionConfigurationValidator() {
    }

    public static void validate(SessionConfiguration configuration) {
        if (configuration == null) {
            throw new BadRequestException("Configuration body is required");
        }

        if (configuration.getMaxParticipants() != null && configuration.getMaxParticipants() <= 0) {
            throw new BadRequestException("maxParticipants must be greater than 0");
        }

        if (configuration.getWaitlistMaxSize() != null && configuration.getWaitlistMaxSize() < 0) {
            throw new BadRequestException("waitlistMaxSize must be greater than or equal to 0");
        }

        if (configuration.getCancellationMinHoursBeforeStart() != null
                && configuration.getCancellationMinHoursBeforeStart() < 0) {
            throw new BadRequestException("cancellationMinHoursBeforeStart must be greater than or equal to 0");
        }

        if (Boolean.TRUE.equals(configuration.getWaitlistEnabled())) {
            if (configuration.getWaitlistMaxSize() != null && configuration.getWaitlistMaxSize() == 0) {
                throw new BadRequestException("waitlistMaxSize must be greater than 0 when waitlist is enabled");
            }
            if (configuration.getWaitlistStrategy() == null) {
                configuration.setWaitlistStrategy(WaitlistStrategy.FIFO);
            }
        }
    }
}
