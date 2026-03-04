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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteActivityScheduleUseCaseTest {

    private DeleteActivityScheduleUseCase useCase;
    private InMemoryActivityScheduleRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new DeleteActivityScheduleUseCase();
        repository = new InMemoryActivityScheduleRepository();
        useCase.activityScheduleRepository = repository;
    }

    @Test
    void shouldSoftDeleteExistingSchedule() {
        ActivitySchedule schedule = baseSchedule();
        schedule.setId(1L);
        schedule.setActive(true);
        repository.store(schedule);

        ActivitySchedule result = useCase.execute(1L);

        assertFalse(result.getActive());
        assertFalse(repository.findById(1L).getActive());
    }

    @Test
    void shouldThrowEntityNotFoundWhenScheduleDoesNotExist() {
        assertThrows(EntityNotFoundException.class, () -> useCase.execute(999L));
    }

    @Test
    void shouldThrowBadRequestWhenIdIsNull() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(null));
        assertEquals("Schedule ID is required", ex.getMessage());
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
