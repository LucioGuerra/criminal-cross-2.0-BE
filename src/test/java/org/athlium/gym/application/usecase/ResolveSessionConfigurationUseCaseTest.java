package org.athlium.gym.application.usecase;

import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.model.WaitlistStrategy;
import org.athlium.gym.domain.repository.SessionConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResolveSessionConfigurationUseCaseTest {

    private ResolveSessionConfigurationUseCase useCase;
    private InMemoryConfigRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new ResolveSessionConfigurationUseCase();
        repository = new InMemoryConfigRepository();
        useCase.sessionConfigurationRepository = repository;
    }

    @Test
    void shouldResolveHierarchyInCorrectOrder() {
        SessionConfiguration orgConfig = new SessionConfiguration();
        orgConfig.setMaxParticipants(20);
        orgConfig.setWaitlistEnabled(false);
        repository.upsertOrganizationConfig(1L, orgConfig);

        SessionConfiguration hqConfig = new SessionConfiguration();
        hqConfig.setWaitlistEnabled(true);
        hqConfig.setWaitlistMaxSize(5);
        repository.upsertHeadquartersConfig(10L, hqConfig);

        SessionConfiguration activityConfig = new SessionConfiguration();
        activityConfig.setMaxParticipants(12);
        activityConfig.setCancellationMinHoursBeforeStart(4);
        repository.upsertActivityConfig(100L, activityConfig);

        SessionConfiguration sessionConfig = new SessionConfiguration();
        sessionConfig.setMaxParticipants(9);
        sessionConfig.setWaitlistStrategy(WaitlistStrategy.FIFO);
        repository.upsertSessionConfig(1000L, sessionConfig);

        SessionConfiguration effective = useCase.execute(1L, 10L, 100L, 1000L);

        assertEquals(9, effective.getMaxParticipants());
        assertEquals(true, effective.getWaitlistEnabled());
        assertEquals(5, effective.getWaitlistMaxSize());
        assertEquals(4, effective.getCancellationMinHoursBeforeStart());
        assertEquals(WaitlistStrategy.FIFO, effective.getWaitlistStrategy());
    }

    private static class InMemoryConfigRepository implements SessionConfigurationRepository {
        private final Map<Long, SessionConfiguration> org = new HashMap<>();
        private final Map<Long, SessionConfiguration> hq = new HashMap<>();
        private final Map<Long, SessionConfiguration> activity = new HashMap<>();
        private final Map<Long, SessionConfiguration> session = new HashMap<>();

        @Override
        public SessionConfiguration upsertOrganizationConfig(Long organizationId, SessionConfiguration configuration) {
            org.put(organizationId, configuration);
            return configuration;
        }

        @Override
        public SessionConfiguration upsertHeadquartersConfig(Long headquartersId, SessionConfiguration configuration) {
            hq.put(headquartersId, configuration);
            return configuration;
        }

        @Override
        public SessionConfiguration upsertActivityConfig(Long activityId, SessionConfiguration configuration) {
            activity.put(activityId, configuration);
            return configuration;
        }

        @Override
        public SessionConfiguration upsertSessionConfig(Long sessionId, SessionConfiguration configuration) {
            session.put(sessionId, configuration);
            return configuration;
        }

        @Override
        public Optional<SessionConfiguration> getOrganizationConfig(Long organizationId) {
            return Optional.ofNullable(org.get(organizationId));
        }

        @Override
        public Optional<SessionConfiguration> getHeadquartersConfig(Long headquartersId) {
            return Optional.ofNullable(hq.get(headquartersId));
        }

        @Override
        public Optional<SessionConfiguration> getActivityConfig(Long activityId) {
            return Optional.ofNullable(activity.get(activityId));
        }

        @Override
        public Optional<SessionConfiguration> getSessionConfig(Long sessionId) {
            return Optional.ofNullable(session.get(sessionId));
        }
    }
}
