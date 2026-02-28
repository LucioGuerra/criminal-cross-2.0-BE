package org.athlium.gym.application.usecase;

import org.athlium.gym.application.usecase.template.OneTimeSessionTemplateBuilder;
import org.athlium.gym.application.usecase.template.SessionTemplateBuilder;
import org.athlium.gym.application.usecase.template.SessionTemplateDirector;
import org.athlium.gym.application.usecase.template.WeeklyRangeSessionTemplateBuilder;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SchedulerType;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.model.WeekDay;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.athlium.shared.domain.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Instant;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateNextWeekSessionsUseCaseTest {

    private GenerateNextWeekSessionsUseCase useCase;
    private InMemoryActivityScheduleRepository scheduleRepository;
    private InMemorySessionRepository sessionRepository;
    private StubResolveConfigUseCase resolveConfigUseCase;
    private GenerateSessionForScheduleUseCase generateSessionForScheduleUseCase;
    private PersistGeneratedSessionUseCase persistGeneratedSessionUseCase;
    private SessionTemplateDirector sessionTemplateDirector;

    @BeforeEach
    void setUp() {
        useCase = new GenerateNextWeekSessionsUseCase();
        scheduleRepository = new InMemoryActivityScheduleRepository();
        sessionRepository = new InMemorySessionRepository();
        resolveConfigUseCase = new StubResolveConfigUseCase();
        generateSessionForScheduleUseCase = new GenerateSessionForScheduleUseCase();
        persistGeneratedSessionUseCase = new PersistGeneratedSessionUseCase();
        sessionTemplateDirector = new SessionTemplateDirector();

        useCase.activityScheduleRepository = scheduleRepository;
        useCase.generateSessionForScheduleUseCase = generateSessionForScheduleUseCase;

        generateSessionForScheduleUseCase.persistGeneratedSessionUseCase = persistGeneratedSessionUseCase;
        generateSessionForScheduleUseCase.resolveSessionConfigurationUseCase = resolveConfigUseCase;
        generateSessionForScheduleUseCase.sessionTemplateDirector = sessionTemplateDirector;
        generateSessionForScheduleUseCase.activityScheduleRepository = scheduleRepository;

        persistGeneratedSessionUseCase.sessionInstanceRepository = sessionRepository;

        List<SessionTemplateBuilder> builders = List.of(
                new WeeklyRangeSessionTemplateBuilder(),
                new OneTimeSessionTemplateBuilder()
        );
        sessionTemplateDirector.setBuildersForTesting(builders);
    }

    @Test
    void shouldCreateSessionsForActiveSchedules() {
        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(1L);
        schedule.setHeadquartersId(10L);
        schedule.setActivityId(200L);
        schedule.setWeekDays(List.of(WeekDay.MONDAY));
        schedule.setStartTime(LocalTime.of(18, 0));
        schedule.setDurationMinutes(60);
        schedule.setActive(true);
        schedule.setSchedulerType(SchedulerType.WEEKLY_RANGE);
        LocalDate today = LocalDate.now();
        schedule.setActiveFrom(today.minusDays(1));
        schedule.setActiveUntil(today.plusMonths(1));
        scheduleRepository.schedules.add(schedule);

        var result = useCase.execute();

        assertEquals(1, result.created());
        assertEquals(0, result.skipped());
        assertEquals(0, result.failed());
        assertEquals(0, result.deactivated());
        assertEquals(1, sessionRepository.saved.size());
        assertEquals(SessionStatus.OPEN, sessionRepository.saved.get(0).getStatus());
    }

    @Test
    void shouldSkipExistingSessionSlots() {
        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(1L);
        schedule.setHeadquartersId(10L);
        schedule.setActivityId(200L);
        schedule.setWeekDays(List.of(WeekDay.TUESDAY));
        schedule.setStartTime(LocalTime.of(10, 0));
        schedule.setDurationMinutes(45);
        schedule.setActive(true);
        schedule.setSchedulerType(SchedulerType.WEEKLY_RANGE);
        LocalDate today = LocalDate.now();
        schedule.setActiveFrom(today.minusDays(1));
        schedule.setActiveUntil(today.plusMonths(1));
        scheduleRepository.schedules.add(schedule);

        sessionRepository.alwaysExists = true;

        var result = useCase.execute();

        assertEquals(0, result.created());
        assertEquals(1, result.skipped());
        assertEquals(0, result.failed());
        assertEquals(0, result.deactivated());
        assertTrue(sessionRepository.saved.isEmpty());
        assertTrue(resolveConfigUseCase.called);
    }

    @Test
    void shouldContinueWhenOneScheduleFails() {
        ActivitySchedule invalid = new ActivitySchedule();
        invalid.setId(1L);
        invalid.setOrganizationId(1L);
        invalid.setHeadquartersId(10L);
        invalid.setActivityId(100L);
        invalid.setWeekDays(List.of(WeekDay.MONDAY));
        invalid.setStartTime(LocalTime.of(8, 0));
        invalid.setDurationMinutes(60);
        invalid.setActive(true);
        invalid.setSchedulerType(SchedulerType.WEEKLY_RANGE);
        LocalDate today = LocalDate.now();
        invalid.setActiveFrom(today.minusDays(1));
        invalid.setActiveUntil(today.plusMonths(1));

        ActivitySchedule valid = new ActivitySchedule();
        valid.setId(2L);
        valid.setOrganizationId(1L);
        valid.setHeadquartersId(10L);
        valid.setActivityId(101L);
        valid.setWeekDays(List.of(WeekDay.TUESDAY));
        valid.setStartTime(LocalTime.of(9, 0));
        valid.setDurationMinutes(45);
        valid.setActive(true);
        valid.setSchedulerType(SchedulerType.WEEKLY_RANGE);
        LocalDate todayForValid = LocalDate.now();
        valid.setActiveFrom(todayForValid.minusDays(1));
        valid.setActiveUntil(todayForValid.plusMonths(1));

        scheduleRepository.schedules.add(invalid);
        scheduleRepository.schedules.add(valid);

        var result = useCase.execute();

        assertEquals(1, result.created());
        assertEquals(0, result.skipped());
        assertEquals(1, result.failed());
        assertEquals(0, result.deactivated());
        assertEquals(1, sessionRepository.saved.size());
    }

    @Test
    void shouldTreatUniqueConstraintRaceAsSkipped() {
        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(1L);
        schedule.setHeadquartersId(10L);
        schedule.setActivityId(200L);
        schedule.setWeekDays(List.of(WeekDay.WEDNESDAY));
        schedule.setStartTime(LocalTime.of(11, 0));
        schedule.setDurationMinutes(60);
        schedule.setActive(true);
        schedule.setSchedulerType(SchedulerType.WEEKLY_RANGE);
        LocalDate today = LocalDate.now();
        schedule.setActiveFrom(today.minusDays(1));
        schedule.setActiveUntil(today.plusMonths(1));
        scheduleRepository.schedules.add(schedule);

        sessionRepository.throwUniqueViolationOnSave = true;

        var result = useCase.execute();

        assertEquals(0, result.created());
        assertEquals(1, result.skipped());
        assertEquals(0, result.failed());
        assertEquals(0, result.deactivated());
    }

    @Test
    void shouldCreateSessionForOneTimeTemplateScheduledInNextWeek() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(1L);
        schedule.setHeadquartersId(10L);
        schedule.setActivityId(300L);
        schedule.setStartTime(LocalTime.of(14, 0));
        schedule.setDurationMinutes(30);
        schedule.setActive(true);
        schedule.setSchedulerType(SchedulerType.ONE_TIME_DISPOSABLE);
        schedule.setScheduledDate(nextMonday.plusDays(2));
        scheduleRepository.schedules.add(schedule);

        var result = useCase.execute();

        assertEquals(1, result.created());
        assertEquals(0, result.skipped());
        assertEquals(0, result.failed());
        assertEquals(1, result.deactivated());
        assertEquals(false, schedule.getActive());
        assertEquals(1, sessionRepository.saved.size());
    }

    @Test
    void shouldDeactivateOneTimeDisposableWhenSessionAlreadyExists() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(1L);
        schedule.setHeadquartersId(10L);
        schedule.setActivityId(350L);
        schedule.setStartTime(LocalTime.of(14, 0));
        schedule.setDurationMinutes(30);
        schedule.setActive(true);
        schedule.setSchedulerType(SchedulerType.ONE_TIME_DISPOSABLE);
        schedule.setScheduledDate(nextMonday.plusDays(3));
        scheduleRepository.schedules.add(schedule);
        sessionRepository.alwaysExists = true;

        var result = useCase.execute();

        assertEquals(0, result.created());
        assertEquals(1, result.skipped());
        assertEquals(0, result.failed());
        assertEquals(1, result.deactivated());
        assertEquals(false, schedule.getActive());
    }

    @Test
    void shouldCreateWeeklyRangeSessionsForMultipleWeekDays() {
        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(1L);
        schedule.setHeadquartersId(10L);
        schedule.setActivityId(301L);
        schedule.setWeekDays(List.of(WeekDay.MONDAY, WeekDay.WEDNESDAY));
        schedule.setStartTime(LocalTime.of(14, 0));
        schedule.setDurationMinutes(30);
        schedule.setActive(true);
        schedule.setSchedulerType(SchedulerType.WEEKLY_RANGE);
        schedule.setActiveFrom(LocalDate.now().minusDays(1));
        schedule.setActiveUntil(LocalDate.now().plusMonths(1));
        scheduleRepository.schedules.add(schedule);

        var result = useCase.execute();

        assertEquals(2, result.created());
        assertEquals(0, result.skipped());
        assertEquals(0, result.failed());
        assertEquals(0, result.deactivated());
        assertEquals(2, sessionRepository.saved.size());
    }

    @Test
    void shouldDeactivateExpiredWeeklyRangeAndSkipProcessing() {
        ActivitySchedule expired = new ActivitySchedule();
        expired.setOrganizationId(1L);
        expired.setHeadquartersId(10L);
        expired.setActivityId(302L);
        expired.setWeekDays(List.of(WeekDay.MONDAY));
        expired.setStartTime(LocalTime.of(8, 0));
        expired.setDurationMinutes(30);
        expired.setActive(true);
        expired.setSchedulerType(SchedulerType.WEEKLY_RANGE);
        expired.setActiveFrom(LocalDate.now().minusMonths(2));
        expired.setActiveUntil(LocalDate.now().minusDays(1));
        scheduleRepository.schedules.add(expired);

        var result = useCase.execute();

        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertEquals(0, result.failed());
        assertEquals(1, result.deactivated());
        assertEquals(false, expired.getActive());
        assertEquals(0, sessionRepository.saved.size());
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
        private boolean throwUniqueViolationOnSave;

        @Override
        public SessionInstance save(SessionInstance sessionInstance) {
            if (throwUniqueViolationOnSave) {
                throw new ConstraintViolationException("duplicate", new SQLException("duplicate key"), "uq_session_instances_slot");
            }
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
            if (Long.valueOf(100L).equals(activityId)) {
                throw new IllegalStateException("Cannot resolve config");
            }
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
