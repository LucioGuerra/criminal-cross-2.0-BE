package org.athlium.gym.presentation.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.CreateActivityScheduleUseCase;
import org.athlium.gym.application.usecase.DeleteActivityScheduleUseCase;
import org.athlium.gym.application.usecase.GenerateNextWeekSessionsUseCase;
import org.athlium.gym.application.usecase.GetActivitySchedulesUseCase;
import org.athlium.gym.application.usecase.UpdateActivityScheduleUseCase;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.presentation.dto.ActivityScheduleRequest;
import org.athlium.gym.presentation.dto.ActivityScheduleResponse;
import org.athlium.gym.presentation.mapper.ActivityScheduleDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityScheduleResourceUnitTest {

    private ActivityScheduleResource resource;
    private StubDeleteActivityScheduleUseCase stubDelete;
    private StubUpdateActivityScheduleUseCase stubUpdate;

    @BeforeEach
    void setUp() {
        resource = new ActivityScheduleResource();

        stubDelete = new StubDeleteActivityScheduleUseCase();
        stubUpdate = new StubUpdateActivityScheduleUseCase();

        resource.deleteActivityScheduleUseCase = stubDelete;
        resource.updateActivityScheduleUseCase = stubUpdate;
        resource.mapper = new StubActivityScheduleDtoMapper();
        resource.createActivityScheduleUseCase = new StubCreateActivityScheduleUseCase();
        resource.getActivitySchedulesUseCase = new StubGetActivitySchedulesUseCase();
        resource.generateNextWeekSessionsUseCase = new StubGenerateNextWeekSessionsUseCase();
    }

    // --- DELETE endpoint tests ---

    @Test
    void shouldReturn200WhenDeleteSucceeds() {
        ActivitySchedule deleted = new ActivitySchedule();
        deleted.setId(1L);
        deleted.setActive(false);
        stubDelete.response = deleted;

        Response response = resource.delete(1L);

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
    }

    @Test
    void shouldReturn404WhenDeleteScheduleNotFound() {
        stubDelete.throwNotFound = true;

        Response response = resource.delete(99L);

        assertEquals(404, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
    }

    @Test
    void shouldReturn400WhenDeleteWithBadInput() {
        stubDelete.throwBadRequest = true;

        Response response = resource.delete(null);

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
    }

    // --- PUT endpoint tests ---

    @Test
    void shouldReturn200WhenUpdateSucceeds() {
        ActivitySchedule updated = new ActivitySchedule();
        updated.setId(1L);
        updated.setDurationMinutes(90);
        stubUpdate.response = updated;

        Response response = resource.update(1L, new ActivityScheduleRequest());

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
    }

    @Test
    void shouldReturn404WhenUpdateScheduleNotFound() {
        stubUpdate.throwNotFound = true;

        Response response = resource.update(99L, new ActivityScheduleRequest());

        assertEquals(404, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
    }

    @Test
    void shouldReturn400WhenUpdateWithBadInput() {
        stubUpdate.throwBadRequest = true;

        Response response = resource.update(1L, new ActivityScheduleRequest());

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
    }

    // --- Stub inner classes ---

    private static class StubDeleteActivityScheduleUseCase extends DeleteActivityScheduleUseCase {
        ActivitySchedule response;
        boolean throwNotFound;
        boolean throwBadRequest;

        @Override
        public ActivitySchedule execute(Long id) {
            if (throwNotFound) {
                throw new EntityNotFoundException("Activity schedule not found");
            }
            if (throwBadRequest) {
                throw new BadRequestException("Schedule ID is required");
            }
            return response;
        }
    }

    private static class StubUpdateActivityScheduleUseCase extends UpdateActivityScheduleUseCase {
        ActivitySchedule response;
        boolean throwNotFound;
        boolean throwBadRequest;

        @Override
        public ActivitySchedule execute(Long id, ActivitySchedule data) {
            if (throwNotFound) {
                throw new EntityNotFoundException("Activity schedule not found");
            }
            if (throwBadRequest) {
                throw new BadRequestException("Schedule data is required");
            }
            return response;
        }
    }

    private static class StubCreateActivityScheduleUseCase extends CreateActivityScheduleUseCase {
        @Override
        public ActivitySchedule execute(ActivitySchedule schedule) {
            return schedule;
        }
    }

    private static class StubGetActivitySchedulesUseCase extends GetActivitySchedulesUseCase {
        @Override
        public List<ActivitySchedule> execute(Long headquartersId) {
            return List.of();
        }
    }

    private static class StubGenerateNextWeekSessionsUseCase extends GenerateNextWeekSessionsUseCase {
        @Override
        public GenerationResult execute() {
            return new GenerationResult(0, 0, 0, 0);
        }
    }

    private static class StubActivityScheduleDtoMapper extends ActivityScheduleDtoMapper {
        @Override
        public ActivityScheduleResponse toResponse(ActivitySchedule schedule) {
            return new ActivityScheduleResponse();
        }

        @Override
        public ActivitySchedule toDomain(ActivityScheduleRequest request) {
            return new ActivitySchedule();
        }

        @Override
        public List<ActivityScheduleResponse> toResponseList(List<ActivitySchedule> schedules) {
            return schedules.stream().map(this::toResponse).toList();
        }
    }
}
