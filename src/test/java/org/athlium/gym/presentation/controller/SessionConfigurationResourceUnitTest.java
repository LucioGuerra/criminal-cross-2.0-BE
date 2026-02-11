package org.athlium.gym.presentation.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.ResolveSessionConfigurationUseCase;
import org.athlium.gym.application.usecase.UpsertActivityConfigUseCase;
import org.athlium.gym.application.usecase.UpsertHeadquartersConfigUseCase;
import org.athlium.gym.application.usecase.UpsertOrganizationConfigUseCase;
import org.athlium.gym.application.usecase.UpsertSessionConfigUseCase;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.model.WaitlistStrategy;
import org.athlium.gym.presentation.dto.SessionConfigurationRequest;
import org.athlium.gym.presentation.mapper.SessionConfigurationDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionConfigurationResourceUnitTest {

    private SessionConfigurationResource resource;
    private StubUpsertOrganizationConfigUseCase upsertOrganization;
    private StubResolveSessionConfigurationUseCase resolve;

    @BeforeEach
    void setUp() {
        resource = new SessionConfigurationResource();
        upsertOrganization = new StubUpsertOrganizationConfigUseCase();
        resolve = new StubResolveSessionConfigurationUseCase();

        resource.upsertOrganizationConfigUseCase = upsertOrganization;
        resource.upsertHeadquartersConfigUseCase = new StubUpsertHeadquartersConfigUseCase();
        resource.upsertActivityConfigUseCase = new StubUpsertActivityConfigUseCase();
        resource.upsertSessionConfigUseCase = new StubUpsertSessionConfigUseCase();
        resource.resolveSessionConfigurationUseCase = resolve;
        resource.mapper = new SessionConfigurationDtoMapper();
    }

    @Test
    void shouldUpsertOrganizationConfig() {
        SessionConfigurationRequest request = new SessionConfigurationRequest();
        request.setMaxParticipants(15);
        request.setWaitlistEnabled(true);
        request.setWaitlistMaxSize(6);
        request.setWaitlistStrategy(WaitlistStrategy.FIFO);

        Response response = resource.upsertOrganizationConfig(1L, request);

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Organization config updated", body.getMessage());
    }

    @Test
    void shouldReturnBadRequestWhenUpsertFails() {
        upsertOrganization.throwBadRequest = true;

        Response response = resource.upsertOrganizationConfig(1L, new SessionConfigurationRequest());

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
    }

    @Test
    void shouldResolveEffectiveConfig() {
        SessionConfiguration config = new SessionConfiguration();
        config.setMaxParticipants(20);
        resolve.response = config;

        Response response = resource.getEffectiveConfig(1L, 10L, 100L, null);

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Effective config resolved", body.getMessage());
    }

    private static class StubUpsertOrganizationConfigUseCase extends UpsertOrganizationConfigUseCase {
        boolean throwBadRequest;

        @Override
        public SessionConfiguration execute(Long organizationId, SessionConfiguration configuration) {
            if (throwBadRequest) {
                throw new BadRequestException("invalid");
            }
            return configuration;
        }
    }

    private static class StubUpsertHeadquartersConfigUseCase extends UpsertHeadquartersConfigUseCase {
        @Override
        public SessionConfiguration execute(Long headquartersId, SessionConfiguration configuration) {
            return configuration;
        }
    }

    private static class StubUpsertActivityConfigUseCase extends UpsertActivityConfigUseCase {
        @Override
        public SessionConfiguration execute(Long activityId, SessionConfiguration configuration) {
            return configuration;
        }
    }

    private static class StubUpsertSessionConfigUseCase extends UpsertSessionConfigUseCase {
        @Override
        public SessionConfiguration execute(Long sessionId, SessionConfiguration configuration) {
            return configuration;
        }
    }

    private static class StubResolveSessionConfigurationUseCase extends ResolveSessionConfigurationUseCase {
        SessionConfiguration response;

        @Override
        public SessionConfiguration execute(Long organizationId, Long headquartersId, Long activityId, Long sessionId) {
            return response;
        }
    }
}
