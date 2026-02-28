package org.athlium.gym.domain.repository;

import org.athlium.gym.domain.model.SessionConfiguration;

import java.util.Optional;

public interface SessionConfigurationRepository {

    SessionConfiguration upsertOrganizationConfig(Long organizationId, SessionConfiguration configuration);

    SessionConfiguration upsertHeadquartersConfig(Long headquartersId, SessionConfiguration configuration);

    SessionConfiguration upsertActivityConfig(Long activityId, SessionConfiguration configuration);

    SessionConfiguration upsertSessionConfig(Long sessionId, SessionConfiguration configuration);

    Optional<SessionConfiguration> getOrganizationConfig(Long organizationId);

    Optional<SessionConfiguration> getHeadquartersConfig(Long headquartersId);

    Optional<SessionConfiguration> getActivityConfig(Long activityId);

    Optional<SessionConfiguration> getSessionConfig(Long sessionId);
}
