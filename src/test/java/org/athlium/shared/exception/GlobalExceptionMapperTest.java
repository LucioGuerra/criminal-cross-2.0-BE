package org.athlium.shared.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.domain.exception.AuthenticationException;
import org.athlium.auth.domain.exception.UserAlreadyExistsException;
import org.athlium.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionMapperTest {

    private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

    @Test
    void shouldMapConstraintViolationsWithPreciseDetails() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<ValidationPayload>> violations = validator.validate(new ValidationPayload(""));
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Validation failed for one or more fields", apiResponse.getMessage());

        Map<?, ?> data = (Map<?, ?>) apiResponse.getData();
        assertEquals("VALIDATION_ERROR", data.get("code"));

        List<?> details = (List<?>) data.get("details");
        assertNotNull(details);
        assertFalse(details.isEmpty());

        Map<?, ?> violationDetail = (Map<?, ?>) details.get(0);
        assertTrue(violationDetail.containsKey("field"));
        assertTrue(violationDetail.containsKey("message"));
    }

    @Test
    void shouldMapBadRequestExceptions() {
        Response response = mapper.toResponse(new BadRequestException("page must be >= 1"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertResponseCodeAndMessage(response, "BAD_REQUEST", "page must be >= 1");
    }

    @Test
    void shouldMapEntityNotFoundExceptions() {
        Response response = mapper.toResponse(new EntityNotFoundException("User", 99L));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertResponseCodeAndMessage(response, "NOT_FOUND", "User with id 99 not found");
    }

    @Test
    void shouldMapForbiddenExceptions() {
        Response response = mapper.toResponse(new ForbiddenException("No permission to cancel this booking"));

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertResponseCodeAndMessage(response, "FORBIDDEN", "No permission to cancel this booking");
    }

    @Test
    void shouldMapUnauthorizedExceptions() {
        Response response = mapper.toResponse(new AuthenticationException("Token has expired"));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertResponseCodeAndMessage(response, "UNAUTHORIZED", "Token has expired");
    }

    @Test
    void shouldMapBusinessDomainExceptions() {
        Response response = mapper.toResponse(new DomainException("Only ORG_ADMIN can update this user"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertResponseCodeAndMessage(response, "DOMAIN_ERROR", "Only ORG_ADMIN can update this user");
    }

    @Test
    void shouldMapUserAlreadyExistsAsConflict() {
        Response response = mapper.toResponse(new UserAlreadyExistsException("User is already registered"));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        assertResponseCodeAndMessage(response, "CONFLICT", "User is already registered");
    }

    @Test
    void shouldMapUnhandledExceptionsWithoutLeakingDetails() {
        Response response = mapper.toResponse(new RuntimeException("db credentials leaked"));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getEntity();
        assertEquals("Internal server error. Please contact support if the issue persists.", apiResponse.getMessage());

        Map<?, ?> data = (Map<?, ?>) apiResponse.getData();
        assertEquals("INTERNAL_ERROR", data.get("code"));
        assertFalse(apiResponse.getMessage().contains("db credentials leaked"));
    }

    private void assertResponseCodeAndMessage(Response response, String code, String message) {
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals(message, apiResponse.getMessage());
        Map<?, ?> data = (Map<?, ?>) apiResponse.getData();
        assertEquals(code, data.get("code"));
    }

    private record ValidationPayload(@NotBlank(message = "name is required") String name) {
    }
}
