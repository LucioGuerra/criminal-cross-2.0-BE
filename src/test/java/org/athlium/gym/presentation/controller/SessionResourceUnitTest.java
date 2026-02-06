package org.athlium.gym.presentation.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.GetSessionByIdUseCase;
import org.athlium.gym.application.usecase.GetSessionsUseCase;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionSource;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.presentation.dto.SessionResponse;
import org.athlium.gym.presentation.mapper.SessionDtoMapper;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionResourceUnitTest {

    private SessionResource resource;
    private StubGetSessionsUseCase getSessionsUseCase;
    private StubGetSessionByIdUseCase getSessionByIdUseCase;

    @BeforeEach
    void setUp() {
        resource = new SessionResource();

        getSessionsUseCase = new StubGetSessionsUseCase();
        getSessionByIdUseCase = new StubGetSessionByIdUseCase();

        resource.getSessionsUseCase = getSessionsUseCase;
        resource.getSessionByIdUseCase = getSessionByIdUseCase;
        resource.sessionDtoMapper = new StubSessionDtoMapper();
    }

    @Test
    void shouldReturnSessionsPage() {
        SessionInstance session = new SessionInstance();
        setField(session, "id", 1L);
        setField(session, "organizationId", 7L);
        setField(session, "headquartersId", 10L);
        setField(session, "activityId", 100L);
        setField(session, "startsAt", Instant.parse("2026-02-10T10:00:00Z"));
        setField(session, "endsAt", Instant.parse("2026-02-10T11:00:00Z"));
        setField(session, "status", SessionStatus.OPEN);
        setField(session, "source", SessionSource.MANUAL);

        getSessionsUseCase.response = new PageResponse<>(List.of(session), 0, 20, 1);

        Response response = resource.getSessions(null, null, null, null, null, null, 1, 20, "startsAt:asc");

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Sessions retrieved successfully", body.getMessage());
    }

    @Test
    void shouldReturnBadRequestWhenFromIsInvalid() {
        Response response = resource.getSessions(null, null, null, null, "bad-date", null, 1, 20, "startsAt:asc");

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
        assertEquals("from must be a valid ISO-8601 instant", body.getMessage());
    }

    @Test
    void shouldReturnNotFoundWhenSessionByIdDoesNotExist() {
        getSessionByIdUseCase.throwNotFound = true;

        Response response = resource.getSessionById(99L);

        assertEquals(404, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
    }

    @Test
    void shouldReturnSessionById() {
        SessionInstance session = new SessionInstance();
        setField(session, "id", 5L);
        setField(session, "organizationId", 2L);
        setField(session, "headquartersId", 3L);
        setField(session, "activityId", 4L);
        setField(session, "startsAt", Instant.parse("2026-02-10T10:00:00Z"));
        setField(session, "endsAt", Instant.parse("2026-02-10T11:00:00Z"));
        setField(session, "status", SessionStatus.OPEN);
        setField(session, "source", SessionSource.MANUAL);
        getSessionByIdUseCase.response = session;

        Response response = resource.getSessionById(5L);

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Session found", body.getMessage());
    }

    private static class StubGetSessionsUseCase extends GetSessionsUseCase {
        PageResponse<SessionInstance> response;

        @Override
        public PageResponse<SessionInstance> execute(
                Long organizationId,
                Long headquartersId,
                Long activityId,
                SessionStatus status,
                Instant from,
                Instant to,
                int page,
                int limit,
                String sort
        ) {
            if (response == null) {
                throw new BadRequestException("No response configured");
            }
            return response;
        }
    }

    private static class StubGetSessionByIdUseCase extends GetSessionByIdUseCase {
        SessionInstance response;
        boolean throwNotFound;

        @Override
        public SessionInstance execute(Long id) {
            if (throwNotFound) {
                throw new EntityNotFoundException("Session", id);
            }
            if (response == null) {
                throw new BadRequestException("No response configured");
            }
            return response;
        }
    }

    private static class StubSessionDtoMapper implements SessionDtoMapper {

        @Override
        public SessionResponse toResponse(SessionInstance session) {
            SessionResponse response = new SessionResponse();
            setField(response, "id", getField(session, "id"));
            setField(response, "organizationId", getField(session, "organizationId"));
            setField(response, "headquartersId", getField(session, "headquartersId"));
            setField(response, "activityId", getField(session, "activityId"));
            setField(response, "startsAt", getField(session, "startsAt"));
            setField(response, "endsAt", getField(session, "endsAt"));
            setField(response, "status", getField(session, "status"));
            setField(response, "source", getField(session, "source"));
            return response;
        }

        @Override
        public List<SessionResponse> toResponseList(List<SessionInstance> sessions) {
            return sessions.stream().map(this::toResponse).toList();
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
