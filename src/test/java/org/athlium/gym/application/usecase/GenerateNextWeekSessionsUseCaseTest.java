package org.athlium.gym.application.usecase;

import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.shared.domain.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateNextWeekSessionsUseCaseTest {

    private GenerateNextWeekSessionsUseCase useCase;
    private InMemoryActivityScheduleRepository scheduleRepository;
    private InMemorySessionRepository sessionRepository;
    private StubResolveConfigUseCase resolveConfigUseCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateNextWeekSessionsUseCase();
        scheduleRepository = new InMemoryActivityScheduleRepository();
        sessionRepository = new InMemorySessionRepository();
        resolveConfigUseCase = new StubResolveConfigUseCase();

        useCase.activityScheduleRepository = scheduleRepository;
        useCase.sessionInstanceRepository = sessionRepository;
        useCase.resolveSessionConfigurationUseCase = resolveConfigUseCase;
    }

    @Test
    void shouldCreateSessionsForActiveSchedules() {
        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(1L);
        schedule.setHeadquartersId(10L);
        schedule.setActivityId(100L);
        schedule.setDayOfWeek(1);
        schedule.setStartTime(LocalTime.of(18, 0));
        schedule.setDurationMinutes(60);
        schedule.setActive(true);
        scheduleRepository.schedules.add(schedule);

        var result = useCase.execute();

        assertEquals(1, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, sessionRepository.saved.size());
        assertEquals(SessionStatus.OPEN, sessionRepository.saved.get(0).getStatus());
    }

    @Test
    void shouldSkipExistingSessionSlots() {
        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(1L);
        schedule.setHeadquartersId(10L);
        schedule.setActivityId(100L);
        schedule.setDayOfWeek(2);
        schedule.setStartTime(LocalTime.of(10, 0));
        schedule.setDurationMinutes(45);
        schedule.setActive(true);
        scheduleRepository.schedules.add(schedule);

        sessionRepository.alwaysExists = true;

        var result = useCase.execute();

        assertEquals(0, result.created());
        assertEquals(1, result.skipped());
        assertTrue(sessionRepository.saved.isEmpty());
        assertFalse(resolveConfigUseCase.called);
    }

    private static class InMemoryActivityScheduleRepository implements ActivityScheduleRepository {
        private final List<ActivitySchedule> schedules = new ArrayList<>();

        @Override
        public ActivitySchedule save(ActivitySchedule schedule) {
            schedules.add(schedule);
            return schedule;
        }

        @Override
        public List<ActivitySchedule> findAllActive() {
            return schedules;
        }

        @Override
        public List<ActivitySchedule> findByHeadquartersId(Long headquartersId) {
            return schedules.stream().filter(s -> headquartersId.equals(s.getHeadquartersId())).toList();
        }
    }

    private static class InMemorySessionRepository implements SessionInstanceRepository {
        private final List<SessionInstance> saved = new ArrayList<>();
        private boolean alwaysExists;

        @Override
        public SessionInstance save(SessionInstance sessionInstance) {
            saved.add(sessionInstance);
            return sessionInstance;
        }

        @Override
        public boolean existsByOrganizationAndHeadquartersAndActivityAndStartsAt(
                Long organizationId,
                Long headquartersId,
                Long activityId,
                Instant startsAt
        ) {
            return alwaysExists;
        }

        @Override
        public Optional<SessionInstance> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public Optional<SessionInstance> findByIdForUpdate(Long id) {
            return Optional.empty();
        }

        @Override
        public PageResponse<SessionInstance> findSessions(
                Long organizationId,
                Long headquartersId,
                Long activityId,
                SessionStatus status,
                Instant from,
                Instant to,
                int page,
                int size,
                boolean sortAscending
        ) {
            return new PageResponse<>(List.of(), page, size, 0);
        }
    }

    private static class StubResolveConfigUseCase extends ResolveSessionConfigurationUseCase {
        private boolean called;

        @Override
        public SessionConfiguration execute(Long organizationId, Long headquartersId, Long activityId, Long sessionId) {
            called = true;
            SessionConfiguration config = new SessionConfiguration();
            config.setMaxParticipants(12);
            config.setWaitlistEnabled(true);
            config.setWaitlistMaxSize(5);
            config.setCancellationMinHoursBeforeStart(2);
            config.setCancellationAllowLateCancel(false);
            return config;
        }
    }
}
