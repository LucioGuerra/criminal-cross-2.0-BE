package org.athlium.gym.application.usecase;

import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetSessionByIdUseCaseTest {

    private GetSessionByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetSessionByIdUseCase();
        useCase.sessionInstanceRepository = new InMemorySessionRepository();
    }

    @Test
    void shouldRejectNonPositiveId() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(0L));
        assertEquals("Session ID must be a positive number", ex.getMessage());
    }

    @Test
    void shouldThrowWhenSessionDoesNotExist() {
        assertThrows(EntityNotFoundException.class, () -> useCase.execute(2L));
    }

    @Test
    void shouldReturnSessionWhenExists() {
        SessionInstance session = useCase.execute(1L);
        assertNotNull(session);
        assertEquals(SessionStatus.OPEN, session.getStatus());
    }

    private static class InMemorySessionRepository implements SessionInstanceRepository {

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
            if (id.equals(1L)) {
                SessionInstance session = new SessionInstance();
                session.setId(1L);
                session.setOrganizationId(10L);
                session.setHeadquartersId(20L);
                session.setActivityId(30L);
                session.setStartsAt(Instant.parse("2026-02-12T10:00:00Z"));
                session.setEndsAt(Instant.parse("2026-02-12T11:00:00Z"));
                session.setStatus(SessionStatus.OPEN);
                return Optional.of(session);
            }
            return Optional.empty();
        }

        @Override
        public Optional<SessionInstance> findByIdForUpdate(Long id) {
            return findById(id);
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
}
