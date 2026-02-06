package org.athlium.gym.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.presentation.dto.SessionConfigurationRequest;
import org.athlium.gym.presentation.dto.SessionConfigurationResponse;

@ApplicationScoped
public class SessionConfigurationDtoMapper {

    public SessionConfiguration toDomain(SessionConfigurationRequest request) {
        if (request == null) {
            return null;
        }
        SessionConfiguration configuration = new SessionConfiguration();
        configuration.setMaxParticipants(request.getMaxParticipants());
        configuration.setWaitlistEnabled(request.getWaitlistEnabled());
        configuration.setWaitlistMaxSize(request.getWaitlistMaxSize());
        configuration.setWaitlistStrategy(request.getWaitlistStrategy());
        configuration.setCancellationMinHoursBeforeStart(request.getCancellationMinHoursBeforeStart());
        configuration.setCancellationAllowLateCancel(request.getCancellationAllowLateCancel());
        return configuration;
    }

    public SessionConfigurationResponse toResponse(SessionConfiguration configuration) {
        SessionConfigurationResponse response = new SessionConfigurationResponse();
        response.setMaxParticipants(configuration.getMaxParticipants());
        response.setWaitlistEnabled(configuration.getWaitlistEnabled());
        response.setWaitlistMaxSize(configuration.getWaitlistMaxSize());
        response.setWaitlistStrategy(configuration.getWaitlistStrategy());
        response.setCancellationMinHoursBeforeStart(configuration.getCancellationMinHoursBeforeStart());
        response.setCancellationAllowLateCancel(configuration.getCancellationAllowLateCancel());
        return response;
    }
}
