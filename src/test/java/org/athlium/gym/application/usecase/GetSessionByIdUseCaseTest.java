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
import java.lang.reflect.Field;
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
        assertEquals(SessionStatus.OPEN, getField(session, "status"));
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
                setField(session, "id", 1L);
                setField(session, "organizationId", 10L);
                setField(session, "headquartersId", 20L);
                setField(session, "activityId", 30L);
                setField(session, "startsAt", Instant.parse("2026-02-12T10:00:00Z"));
                setField(session, "endsAt", Instant.parse("2026-02-12T11:00:00Z"));
                setField(session, "status", SessionStatus.OPEN);
                return Optional.of(session);
            }
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

    private static Object getField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed reading field " + fieldName, e);
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed setting field " + fieldName, e);
        }
    }
}
