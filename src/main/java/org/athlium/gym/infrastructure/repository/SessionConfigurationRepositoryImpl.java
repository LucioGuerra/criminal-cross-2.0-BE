package org.athlium.gym.infrastructure.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    void ensureUniqueIndexes() {
        organizationConfigRepository.mongoCollection().createIndex(
                Indexes.ascending("organizationId"),
                new IndexOptions().unique(true).name("uq_gym_config_organization_id")
        );
        headquartersConfigRepository.mongoCollection().createIndex(
                Indexes.ascending("headquartersId"),
                new IndexOptions().unique(true).name("uq_branch_config_headquarters_id")
        );
        activityConfigRepository.mongoCollection().createIndex(
                Indexes.ascending("activityId"),
                new IndexOptions().unique(true).name("uq_activity_config_activity_id")
        );
        sessionConfigRepository.mongoCollection().createIndex(
                Indexes.ascending("sessionId"),
                new IndexOptions().unique(true).name("uq_session_overrides_session_id")
        );
    }

    @Override
    public SessionConfiguration upsertOrganizationConfig(Long organizationId, SessionConfiguration configuration) {
        SessionConfigurationDocument configDocument = toDocument(configuration);
        organizationConfigRepository.mongoCollection().updateOne(
                Filters.eq("organizationId", organizationId),
                Updates.combine(
                        Updates.set("organizationId", organizationId),
                        Updates.set("configuration", configDocument)
                ),
                new UpdateOptions().upsert(true)
        );

        OrganizationConfigDocument document = organizationConfigRepository.find("organizationId", organizationId).firstResult();
        return toDomain(document.configuration);
    }

    @Override
    public SessionConfiguration upsertHeadquartersConfig(Long headquartersId, SessionConfiguration configuration) {
        SessionConfigurationDocument configDocument = toDocument(configuration);
        headquartersConfigRepository.mongoCollection().updateOne(
                Filters.eq("headquartersId", headquartersId),
                Updates.combine(
                        Updates.set("headquartersId", headquartersId),
                        Updates.set("configuration", configDocument)
                ),
                new UpdateOptions().upsert(true)
        );

        HeadquartersConfigDocument document = headquartersConfigRepository.find("headquartersId", headquartersId).firstResult();
        return toDomain(document.configuration);
    }

    @Override
    public SessionConfiguration upsertActivityConfig(Long activityId, SessionConfiguration configuration) {
        SessionConfigurationDocument configDocument = toDocument(configuration);
        activityConfigRepository.mongoCollection().updateOne(
                Filters.eq("activityId", activityId),
                Updates.combine(
                        Updates.set("activityId", activityId),
                        Updates.set("configuration", configDocument)
                ),
                new UpdateOptions().upsert(true)
        );

        ActivityConfigDocument document = activityConfigRepository.find("activityId", activityId).firstResult();
        return toDomain(document.configuration);
    }

    @Override
    public SessionConfiguration upsertSessionConfig(Long sessionId, SessionConfiguration configuration) {
        SessionConfigurationDocument configDocument = toDocument(configuration);
        sessionConfigRepository.mongoCollection().updateOne(
                Filters.eq("sessionId", sessionId),
                Updates.combine(
                        Updates.set("sessionId", sessionId),
                        Updates.set("configuration", configDocument)
                ),
                new UpdateOptions().upsert(true)
        );

        SessionConfigDocument document = sessionConfigRepository.find("sessionId", sessionId).firstResult();
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
