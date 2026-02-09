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
        session.setId(1L);
        session.setOrganizationId(7L);
        session.setHeadquartersId(10L);
        session.setActivityId(100L);
        session.setStartsAt(Instant.parse("2026-02-10T10:00:00Z"));
        session.setEndsAt(Instant.parse("2026-02-10T11:00:00Z"));
        session.setStatus(SessionStatus.OPEN);
        session.setSource(SessionSource.MANUAL);

        getSessionsUseCase.response = new PageResponse<>(List.of(session), 0, 20, 1);

        Response response = resource.getSessions(null, null, null, null, null, null, null, null, 1, 20, "startsAt:asc");

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Sessions retrieved successfully", body.getMessage());
    }

    @Test
    void shouldReturnBadRequestWhenFromIsInvalid() {
        Response response = resource.getSessions(null, null, null, null, null, null, "bad-date", null, 1, 20, "startsAt:asc");

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
        session.setId(5L);
        session.setOrganizationId(2L);
        session.setHeadquartersId(3L);
        session.setActivityId(4L);
        session.setStartsAt(Instant.parse("2026-02-10T10:00:00Z"));
        session.setEndsAt(Instant.parse("2026-02-10T11:00:00Z"));
        session.setStatus(SessionStatus.OPEN);
        session.setSource(SessionSource.MANUAL);
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
            return new SessionResponse();
        }

        @Override
        public List<SessionResponse> toResponseList(List<SessionInstance> sessions) {
            return sessions.stream().map(this::toResponse).toList();
        }
    }
}
