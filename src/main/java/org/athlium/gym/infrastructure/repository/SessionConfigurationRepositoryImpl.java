package org.athlium.gym.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.repository.SessionConfigurationRepository;
import org.athlium.gym.infrastructure.document.ActivityConfigDocument;
import org.athlium.gym.infrastructure.document.HeadquartersConfigDocument;
import org.athlium.gym.infrastructure.document.OrganizationConfigDocument;
import org.athlium.gym.infrastructure.document.SessionConfigDocument;
import org.athlium.gym.infrastructure.document.SessionConfigurationDocument;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class SessionConfigurationRepositoryImpl implements SessionConfigurationRepository {

    @Inject
    OrganizationConfigPanacheRepository organizationConfigRepository;

    @Inject
    HeadquartersConfigPanacheRepository headquartersConfigRepository;

    @Inject
    ActivityConfigPanacheRepository activityConfigRepository;

    @Inject
    SessionConfigPanacheRepository sessionConfigRepository;

    @Override
    public SessionConfiguration upsertOrganizationConfig(Long organizationId, SessionConfiguration configuration) {
        OrganizationConfigDocument document = organizationConfigRepository.find("organizationId", organizationId).firstResult();
        if (document == null) {
            document = new OrganizationConfigDocument();
            document.organizationId = organizationId;
        }
        document.configuration = toDocument(configuration);
        organizationConfigRepository.persistOrUpdate(document);
        return toDomain(document.configuration);
    }

    @Override
    public SessionConfiguration upsertHeadquartersConfig(Long headquartersId, SessionConfiguration configuration) {
        HeadquartersConfigDocument document = headquartersConfigRepository.find("headquartersId", headquartersId).firstResult();
        if (document == null) {
            document = new HeadquartersConfigDocument();
            document.headquartersId = headquartersId;
        }
        document.configuration = toDocument(configuration);
        headquartersConfigRepository.persistOrUpdate(document);
        return toDomain(document.configuration);
    }

    @Override
    public SessionConfiguration upsertActivityConfig(Long activityId, SessionConfiguration configuration) {
        ActivityConfigDocument document = activityConfigRepository.find("activityId", activityId).firstResult();
        if (document == null) {
            document = new ActivityConfigDocument();
            document.activityId = activityId;
        }
        document.configuration = toDocument(configuration);
        activityConfigRepository.persistOrUpdate(document);
        return toDomain(document.configuration);
    }

    @Override
    public SessionConfiguration upsertSessionConfig(Long sessionId, SessionConfiguration configuration) {
        SessionConfigDocument document = sessionConfigRepository.find("sessionId", sessionId).firstResult();
        if (document == null) {
            document = new SessionConfigDocument();
            document.sessionId = sessionId;
        }
        document.configuration = toDocument(configuration);
        sessionConfigRepository.persistOrUpdate(document);
        return toDomain(document.configuration);
    }

    @Override
    public Optional<SessionConfiguration> getOrganizationConfig(Long organizationId) {
        OrganizationConfigDocument document = organizationConfigRepository.find("organizationId", organizationId).firstResult();
        return Optional.ofNullable(document).map(value -> toDomain(value.configuration));
    }

    @Override
    public Optional<SessionConfiguration> getHeadquartersConfig(Long headquartersId) {
        HeadquartersConfigDocument document = headquartersConfigRepository.find("headquartersId", headquartersId).firstResult();
        return Optional.ofNullable(document).map(value -> toDomain(value.configuration));
    }

    @Override
    public Optional<SessionConfiguration> getActivityConfig(Long activityId) {
        ActivityConfigDocument document = activityConfigRepository.find("activityId", activityId).firstResult();
        return Optional.ofNullable(document).map(value -> toDomain(value.configuration));
    }

    @Override
    public Optional<SessionConfiguration> getSessionConfig(Long sessionId) {
        SessionConfigDocument document = sessionConfigRepository.find("sessionId", sessionId).firstResult();
        return Optional.ofNullable(document).map(value -> toDomain(value.configuration));
    }

    private SessionConfigurationDocument toDocument(SessionConfiguration configuration) {
        SessionConfigurationDocument document = new SessionConfigurationDocument();
        document.maxParticipants = configuration.getMaxParticipants();
        document.waitlistEnabled = configuration.getWaitlistEnabled();
        document.waitlistMaxSize = configuration.getWaitlistMaxSize();
        document.waitlistStrategy = configuration.getWaitlistStrategy();
        document.cancellationMinHoursBeforeStart = configuration.getCancellationMinHoursBeforeStart();
        document.cancellationAllowLateCancel = configuration.getCancellationAllowLateCancel();
        document.updatedAt = Instant.now();
        return document;
    }

    private SessionConfiguration toDomain(SessionConfigurationDocument document) {
        SessionConfiguration configuration = new SessionConfiguration();
        configuration.setMaxParticipants(document.maxParticipants);
        configuration.setWaitlistEnabled(document.waitlistEnabled);
        configuration.setWaitlistMaxSize(document.waitlistMaxSize);
        configuration.setWaitlistStrategy(document.waitlistStrategy);
        configuration.setCancellationMinHoursBeforeStart(document.cancellationMinHoursBeforeStart);
        configuration.setCancellationAllowLateCancel(document.cancellationAllowLateCancel);
        return configuration;
    }
}
