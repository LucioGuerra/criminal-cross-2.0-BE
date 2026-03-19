package org.athlium.gym.application.usecase;

import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateActivityScheduleUseCaseTest {

    private UpdateActivityScheduleUseCase useCase;
    private InMemoryActivityScheduleRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new UpdateActivityScheduleUseCase();
        repository = new InMemoryActivityScheduleRepository();
        useCase.activityScheduleRepository = repository;
    }

    @Test
    void shouldUpdateMutableFieldsOnExistingSchedule() {
        ActivitySchedule existing = baseSchedule();
        existing.setId(1L);
        existing.setStartTime(LocalTime.of(18, 0));
        existing.setDurationMinutes(60);
        existing.setDayOfWeek(1);
        repository.store(existing);

        ActivitySchedule updatedData = new ActivitySchedule();
        updatedData.setStartTime(LocalTime.of(20, 0));
        updatedData.setDurationMinutes(90);
        updatedData.setDayOfWeek(3);

        ActivitySchedule result = useCase.execute(1L, updatedData);

        assertEquals(LocalTime.of(20, 0), result.getStartTime());
        assertEquals(90, result.getDurationMinutes());
        assertEquals(3, result.getDayOfWeek());
    }

    @Test
    void shouldPreserveIdentityFieldsOnUpdate() {
        ActivitySchedule existing = baseSchedule();
        existing.setId(1L);
        existing.setOrganizationId(10L);
        existing.setHeadquartersId(20L);
        existing.setActivityId(30L);
        repository.store(existing);

        ActivitySchedule updatedData = new ActivitySchedule();
        updatedData.setOrganizationId(999L);
        updatedData.setHeadquartersId(888L);
        updatedData.setStartTime(LocalTime.of(9, 0));

        ActivitySchedule result = useCase.execute(1L, updatedData);

        assertEquals(10L, result.getOrganizationId());
        assertEquals(20L, result.getHeadquartersId());
        assertEquals(30L, result.getActivityId());
        assertEquals(LocalTime.of(9, 0), result.getStartTime());
    }

    @Test
    void shouldThrowBadRequestWhenOnlyIdentityFieldsAreProvided() {
        ActivitySchedule existing = baseSchedule();
        existing.setId(1L);
        existing.setActivityId(30L);
        repository.store(existing);

        ActivitySchedule updatedData = new ActivitySchedule();
        updatedData.setOrganizationId(999L);
        updatedData.setHeadquartersId(888L);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(1L, updatedData));
        assertEquals("No updatable fields were provided", ex.getMessage());

        ActivitySchedule stored = repository.findById(1L);
        assertEquals(30L, stored.getActivityId());
    }

    @Test
    void shouldAllowUpdatingActivityId() {
        ActivitySchedule existing = baseSchedule();
        existing.setId(1L);
        existing.setActivityId(30L);
        repository.store(existing);

        ActivitySchedule updatedData = new ActivitySchedule();
        updatedData.setActivityId(99L);

        ActivitySchedule result = useCase.execute(1L, updatedData);

        assertEquals(99L, result.getActivityId());
    }

    @Test
    void shouldThrowEntityNotFoundWhenScheduleDoesNotExist() {
        ActivitySchedule updatedData = new ActivitySchedule();
        updatedData.setStartTime(LocalTime.of(10, 0));

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(999L, updatedData));
    }

    @Test
    void shouldThrowBadRequestWhenIdIsNull() {
        ActivitySchedule updatedData = new ActivitySchedule();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(null, updatedData));
        assertEquals("Schedule ID is required", ex.getMessage());
    }

    @Test
    void shouldThrowBadRequestWhenUpdatedDataIsNull() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(1L, null));
        assertEquals("Schedule data is required", ex.getMessage());
    }

    @Test
    void shouldThrowBadRequestWhenNoUpdatableFieldsAreProvided() {
        ActivitySchedule existing = baseSchedule();
        existing.setId(1L);
        repository.store(existing);

        ActivitySchedule updatedData = new ActivitySchedule();
        updatedData.setOrganizationId(999L);
        updatedData.setHeadquartersId(888L);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(1L, updatedData));
        assertEquals("No updatable fields were provided", ex.getMessage());
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
        private final Map<Long, ActivitySchedule> storage = new HashMap<>();

        void store(ActivitySchedule schedule) {
            storage.put(schedule.getId(), schedule);
        }

        @Override
        public ActivitySchedule save(ActivitySchedule schedule) {
            storage.put(schedule.getId(), schedule);
            return schedule;
        }

        @Override
        public ActivitySchedule findById(Long id) {
            return storage.get(id);
        }

        @Override
        public List<ActivitySchedule> findAllActive() {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public List<ActivitySchedule> findByHeadquartersId(Long headquartersId) {
            throw new UnsupportedOperationException("Not used in this test");
        }
    }
}
