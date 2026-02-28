package org.athlium.gym.application.usecase;

import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SchedulerType;
import org.athlium.gym.domain.model.SessionTemplateType;
import org.athlium.gym.domain.model.WeekDay;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateActivityScheduleUseCaseTest {

    private CreateActivityScheduleUseCase useCase;
    private InMemoryActivityScheduleRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new CreateActivityScheduleUseCase();
        repository = new InMemoryActivityScheduleRepository();
        useCase.activityScheduleRepository = repository;
    }

    @Test
    void shouldCreateWeeklyRangeTemplateWithoutDateRange() {
        ActivitySchedule schedule = baseSchedule();
        schedule.setSchedulerType(SchedulerType.WEEKLY_RANGE);
        schedule.setWeekDays(List.of(WeekDay.TUESDAY));

        ActivitySchedule created = useCase.execute(schedule);

        assertEquals(SchedulerType.WEEKLY_RANGE, created.getSchedulerType());
        assertEquals(List.of(WeekDay.TUESDAY), created.getWeekDays());
        assertEquals(1, repository.saved.size());
    }

    @Test
    void shouldCreateOneTimeDisposableTemplateAndDeriveDayOfWeek() {
        ActivitySchedule schedule = baseSchedule();
        schedule.setSchedulerType(SchedulerType.ONE_TIME_DISPOSABLE);
        schedule.setScheduledDate(LocalDate.now().plusDays(7));

        ActivitySchedule created = useCase.execute(schedule);

        assertEquals(SchedulerType.ONE_TIME_DISPOSABLE, created.getSchedulerType());
        assertEquals(SessionTemplateType.ONE_TIME_DISPOSABLE, created.getTemplateType());
        assertEquals(schedule.getScheduledDate().getDayOfWeek().getValue(), created.getDayOfWeek());
        assertEquals(1, repository.saved.size());
    }

    @Test
    void shouldRejectWeeklyRangeWhenOnlyOneDateBoundaryIsProvided() {
        ActivitySchedule schedule = baseSchedule();
        schedule.setSchedulerType(SchedulerType.WEEKLY_RANGE);
        schedule.setWeekDays(List.of(WeekDay.WEDNESDAY));
        schedule.setActiveFrom(LocalDate.now());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(schedule));

        assertEquals("activeFrom and activeUntil must be provided together for WEEKLY_RANGE templates", ex.getMessage());
    }

    @Test
    void shouldAcceptLegacyTemplateTypeAndDayOfWeekCompatibility() {
        ActivitySchedule schedule = baseSchedule();
        schedule.setTemplateType(SessionTemplateType.ONE_TIME);
        schedule.setScheduledDate(LocalDate.now().plusDays(2));

        ActivitySchedule created = useCase.execute(schedule);

        assertEquals(SchedulerType.ONE_TIME_DISPOSABLE, created.getSchedulerType());
        assertEquals(SessionTemplateType.ONE_TIME_DISPOSABLE, created.getTemplateType());
        assertEquals(1, created.getWeekDays().size());
    }

    private ActivitySchedule baseSchedule() {
        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setOrganizationId(1L);
        schedule.setHeadquartersId(2L);
        schedule.setActivityId(3L);
        schedule.setStartTime(LocalTime.of(18, 0));
        schedule.setDurationMinutes(60);
        schedule.setActive(true);
        return schedule;
    }

    private static class InMemoryActivityScheduleRepository implements ActivityScheduleRepository {
        private final List<ActivitySchedule> saved = new ArrayList<>();

        @Override
        public ActivitySchedule save(ActivitySchedule schedule) {
            saved.add(schedule);
            return schedule;
        }

        @Override
        public List<ActivitySchedule> findAllActive() {
            return saved;
        }

        @Override
        public List<ActivitySchedule> findByHeadquartersId(Long headquartersId) {
            return saved.stream().filter(s -> headquartersId.equals(s.getHeadquartersId())).toList();
        }
    }
}
