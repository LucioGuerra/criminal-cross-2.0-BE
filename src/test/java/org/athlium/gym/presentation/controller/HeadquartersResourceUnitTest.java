package org.athlium.gym.presentation.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.GetActivitiesUseCase;
import org.athlium.gym.application.usecase.GetAllHeadquartersUseCase;
import org.athlium.gym.application.usecase.GetHeadquartersUseCase;
import org.athlium.gym.application.usecase.GetSessionsUseCase;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.presentation.dto.ActivityResponse;
import org.athlium.gym.presentation.dto.HeadquartersInput;
import org.athlium.gym.presentation.dto.HeadquartersResponse;
import org.athlium.gym.presentation.dto.SessionResponse;
import org.athlium.gym.presentation.mapper.ActivityDtoMapper;
import org.athlium.gym.presentation.mapper.HeadquartersDtoMapper;
import org.athlium.gym.presentation.mapper.SessionDtoMapper;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeadquartersResourceUnitTest {

    private HeadquartersResource resource;
    private StubGetAllHeadquartersUseCase getAllHeadquartersUseCase;
    private StubGetHeadquartersUseCase getHeadquartersUseCase;
    private StubGetActivitiesUseCase getActivitiesUseCase;
    private StubGetSessionsUseCase getSessionsUseCase;

    @BeforeEach
    void setUp() {
        resource = new HeadquartersResource();

        getAllHeadquartersUseCase = new StubGetAllHeadquartersUseCase();
        getHeadquartersUseCase = new StubGetHeadquartersUseCase();
        getActivitiesUseCase = new StubGetActivitiesUseCase();
        getSessionsUseCase = new StubGetSessionsUseCase();

        resource.getAllHeadquartersUseCase = getAllHeadquartersUseCase;
        resource.getHeadquartersUseCase = getHeadquartersUseCase;
        resource.getActivitiesUseCase = getActivitiesUseCase;
        resource.getSessionsUseCase = getSessionsUseCase;
        resource.mapper = new StubHeadquartersDtoMapper();
        resource.activityDtoMapper = new StubActivityDtoMapper();
        resource.sessionDtoMapper = new StubSessionDtoMapper();
    }

    @Test
    void shouldIncludeActivitiesAndSessionsWhenListingHeadquarters() {
        Headquarters headquarters = Headquarters.builder().id(10L).organizationId(1L).name("HQ A").build();
        getAllHeadquartersUseCase.response = List.of(headquarters);

        Activity activity = Activity.builder().id(100L).name("Yoga").description("Morning").hqId(10L).build();
        getActivitiesUseCase.response = List.of(activity);

        SessionInstance session = new SessionInstance();
        session.setId(1000L);
        session.setActivityId(100L);
        session.setStartsAt(Instant.parse("2026-03-10T09:00:00Z"));
        getSessionsUseCase.response = new PageResponse<>(List.of(session), 0, 100, 1);

        Response response = resource.getHeadquarters(null);

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        List<?> list = (List<?>) body.getData();
        HeadquartersResponse item = (HeadquartersResponse) list.get(0);
        assertEquals(1, item.getActivities().size());
        assertEquals("Yoga", item.getActivities().get(0).getName());
        assertEquals(1, item.getActivities().get(0).getSessions().size());
        assertEquals(1000L, item.getActivities().get(0).getSessions().get(0).getId());
    }

    @Test
    void shouldIncludeActivitiesAndSessionsWhenGettingHeadquartersById() {
        Headquarters headquarters = Headquarters.builder().id(11L).organizationId(1L).name("HQ B").build();
        getHeadquartersUseCase.response = headquarters;

        Activity activity = Activity.builder().id(200L).name("Pilates").description("Core").hqId(11L).build();
        getActivitiesUseCase.response = List.of(activity);

        SessionInstance session = new SessionInstance();
        session.setId(2000L);
        session.setActivityId(200L);
        getSessionsUseCase.response = new PageResponse<>(List.of(session), 0, 100, 1);

        Response response = resource.getHeadquarter(11L);

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        HeadquartersResponse item = (HeadquartersResponse) body.getData();
        assertNotNull(item.getActivities());
        assertEquals(1, item.getActivities().size());
        assertEquals(1, item.getActivities().get(0).getSessions().size());
        assertEquals(2000L, item.getActivities().get(0).getSessions().get(0).getId());
    }

    private static class StubGetAllHeadquartersUseCase extends GetAllHeadquartersUseCase {
        List<Headquarters> response = List.of();

        @Override
        public List<Headquarters> execute() {
            return response;
        }
    }

    private static class StubGetHeadquartersUseCase extends GetHeadquartersUseCase {
        Headquarters response;

        @Override
        public Headquarters execute(Long id) {
            return response;
        }
    }

    private static class StubGetActivitiesUseCase extends GetActivitiesUseCase {
        List<Activity> response = List.of();

        @Override
        public List<Activity> executeAllByHeadquarter(Long hqId, Boolean isActive) {
            return response;
        }
    }

    private static class StubGetSessionsUseCase extends GetSessionsUseCase {
        PageResponse<SessionInstance> response = new PageResponse<>(List.of(), 0, 100, 0);

        @Override
        public PageResponse<SessionInstance> execute(
                Long organizationId,
                Long headquartersId,
                Long activityId,
                org.athlium.gym.domain.model.SessionStatus status,
                Instant from,
                Instant to,
                int page,
                int limit,
                String sort
        ) {
            return response;
        }
    }

    private static class StubHeadquartersDtoMapper implements HeadquartersDtoMapper {

        @Override
        public Headquarters toDomain(HeadquartersInput input) {
            Headquarters headquarters = new Headquarters();
            headquarters.setOrganizationId(input.getOrganizationId());
            headquarters.setName(input.getName());
            return headquarters;
        }

        @Override
        public HeadquartersResponse toResponse(Headquarters domain) {
            HeadquartersResponse response = new HeadquartersResponse();
            response.setId(domain.getId());
            response.setOrganizationId(domain.getOrganizationId());
            response.setName(domain.getName());
            return response;
        }
    }

    private static class StubActivityDtoMapper implements ActivityDtoMapper {

        @Override
        public Activity toDomain(org.athlium.gym.presentation.dto.ActivityInput input) {
            return null;
        }

        @Override
        public Activity toDomain(org.athlium.gym.presentation.dto.ActivityUpdateInput input) {
            return null;
        }

        @Override
        public ActivityResponse toResponse(Activity activity) {
            ActivityResponse response = new ActivityResponse();
            response.setId(activity.getId());
            response.setName(activity.getName());
            response.setDescription(activity.getDescription());
            response.setHqId(activity.getHqId());
            response.setIsActive(activity.getIsActive());
            return response;
        }

        @Override
        public List<ActivityResponse> toResponseList(List<Activity> activities) {
            return activities.stream().map(this::toResponse).toList();
        }

        @Override
        public org.athlium.gym.presentation.dto.ActivityPageResponse toPageResponse(
                org.athlium.shared.domain.PageResponse<Activity> pageResponse
        ) {
            return null;
        }
    }

    private static class StubSessionDtoMapper implements SessionDtoMapper {

        @Override
        public SessionResponse toResponse(SessionInstance session) {
            SessionResponse response = new SessionResponse();
            response.setId(session.getId());
            return response;
        }

        @Override
        public List<SessionResponse> toResponseList(List<SessionInstance> sessions) {
            return sessions.stream().map(this::toResponse).toList();
        }
    }
}
