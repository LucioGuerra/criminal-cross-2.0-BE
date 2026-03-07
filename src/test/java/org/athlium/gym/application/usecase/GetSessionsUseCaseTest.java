package org.athlium.gym.application.usecase;

import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetSessionsUseCaseTest {

    private GetSessionsUseCase useCase;
    private InMemorySessionRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new GetSessionsUseCase();
        repository = new InMemorySessionRepository();
        useCase.sessionInstanceRepository = repository;
        useCase.activityRepository = new StubActivityRepository();
    }

    @Test
    void shouldRejectInvalidSort() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                useCase.execute(null, null, null, null, null, null, 1, 20, "id:asc")
        );

        assertEquals("sort must be startsAt:asc or startsAt:desc", ex.getMessage());
    }

    @Test
    void shouldRejectInvalidDateRange() {
        Instant from = Instant.parse("2026-02-10T10:00:00Z");
        Instant to = Instant.parse("2026-02-09T10:00:00Z");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                useCase.execute(null, null, null, null, from, to, 1, 20, "startsAt:asc")
        );

        assertEquals("from must be less than or equal to to", ex.getMessage());
    }

    @Test
    void shouldRequestDescendingSortAndZeroBasedPage() {
        PageResponse<SessionInstance> response = useCase.execute(
                1L,
                2L,
                3L,
                SessionStatus.OPEN,
                null,
                null,
                2,
                20,
                "startsAt:desc"
        );

        assertEquals(1, repository.capturedPage);
        assertFalse(repository.capturedSortAscending);
        assertEquals(1, response.getPage());
        assertEquals(0, response.getTotalElements());
    }

    @Test
    void shouldIncludeActivityInReturnedSessions() {
        SessionInstance session = new SessionInstance();
        session.setActivityId(3L);
        repository.responseContent = List.of(session);

        PageResponse<SessionInstance> response = useCase.execute(
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                20,
                "startsAt:asc"
        );

        assertEquals("Yoga", response.getContent().getFirst().getActivity().getName());
    }

    private static class InMemorySessionRepository implements SessionInstanceRepository {
        int capturedPage;
        boolean capturedSortAscending;
        List<SessionInstance> responseContent = List.of();

        @Override
        public SessionInstance save(SessionInstance sessionInstance) {
            return sessionInstance;
        }

        @Override
        public boolean existsByOrganizationAndHeadquartersAndActivityAndStartsAt(
                Long organizationId,
                Long headquartersId,
                Long activityId,
                Instant startsAt
        ) {
            return false;
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
            this.capturedPage = page;
            this.capturedSortAscending = sortAscending;
            return new PageResponse<>(responseContent, page, size, responseContent.size());
        }
    }

    private static class StubActivityRepository implements ActivityRepository {

        @Override
        public Activity save(Activity activity) {
            return activity;
        }

        @Override
        public Activity findById(Long id) {
            return null;
        }

        @Override
        public Map<Long, Activity> findByIds(List<Long> ids) {
            Activity activity = new Activity();
            activity.setId(3L);
            activity.setName("Yoga");
            return Map.of(3L, activity);
        }

        @Override
        public Activity update(Activity activity) {
            return activity;
        }

        @Override
        public void delete(Long id) {
        }

        @Override
        public org.athlium.shared.domain.PageResponse<Activity> findPagedByHqId(
                Long hqId,
                Boolean isActive,
                io.quarkus.panache.common.Page page
        ) {
            return null;
        }

        @Override
        public List<Activity> findAllByHqId(Long hqId, Boolean isActive) {
            return List.of();
        }

        @Override
        public org.athlium.shared.domain.PageResponse<Activity> findByNameAndHqId(
                String name,
                Long hqId,
                io.quarkus.panache.common.Page page
        ) {
            return null;
        }
    }
}
