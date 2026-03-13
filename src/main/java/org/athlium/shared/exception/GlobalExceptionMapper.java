package org.athlium.shared.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.athlium.auth.domain.exception.AuthenticationException;
import org.athlium.auth.domain.exception.InvalidRefreshTokenException;
import org.athlium.auth.domain.exception.UnauthorizedException;
import org.athlium.auth.domain.exception.UserAlreadyExistsException;
import org.athlium.shared.dto.ApiResponse;
import org.jboss.logging.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);
    private static final String FALLBACK_ERROR_MESSAGE = "Internal server error. Please contact support if the issue persists.";

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            List<Map<String, String>> violations = constraintViolationException.getConstraintViolations().stream()
                    .map(violation -> Map.of(
                            "field", violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "request",
                            "message", violation.getMessage() != null ? violation.getMessage() : "Invalid value"
                    ))
                    .toList();
            return buildErrorResponse(Response.Status.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed for one or more fields", violations);
        }

        if (exception instanceof BadRequestException
                || exception instanceof jakarta.ws.rs.BadRequestException
                || exception instanceof IllegalArgumentException
                || exception instanceof ValidationException) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "BAD_REQUEST", resolveMessage(exception, "Invalid request"), null);
        }

        if (exception instanceof EntityNotFoundException || exception instanceof NotFoundException) {
            return buildErrorResponse(Response.Status.NOT_FOUND, "NOT_FOUND", resolveMessage(exception, "Resource not found"), null);
        }

        if (exception instanceof ForbiddenException || exception instanceof UnauthorizedException) {
            return buildErrorResponse(Response.Status.FORBIDDEN, "FORBIDDEN", resolveMessage(exception, "You do not have permission to perform this action"), null);
        }

        if (exception instanceof AuthenticationException
                || exception instanceof InvalidRefreshTokenException
                || exception instanceof NotAuthorizedException) {
            return buildErrorResponse(Response.Status.UNAUTHORIZED, "UNAUTHORIZED", resolveMessage(exception, "Authentication is required"), null);
        }

        if (exception instanceof UserAlreadyExistsException) {
            return buildErrorResponse(Response.Status.CONFLICT, "CONFLICT", resolveMessage(exception, "The resource already exists"), null);
        }

        if (exception instanceof DomainException) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "DOMAIN_ERROR", resolveMessage(exception, "Business rule validation failed"), null);
        }

        if (exception instanceof WebApplicationException webApplicationException) {
            Response.StatusType statusInfo = webApplicationException.getResponse().getStatusInfo();
            String fallback = statusInfo != null ? statusInfo.getReasonPhrase() : "Request failed";
            return buildErrorResponse(webApplicationException.getResponse().getStatus(), "HTTP_ERROR", resolveMessage(exception, fallback), null);
        }

        LOG.error("Unhandled exception caught by global exception mapper", exception);
        return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", FALLBACK_ERROR_MESSAGE, null);
    }

    private Response buildErrorResponse(Response.Status status, String code, String message, Object details) {
        return buildErrorResponse(status.getStatusCode(), code, message, details);
    }

    private Response buildErrorResponse(int statusCode, String code, String message, Object details) {
        Map<String, Object> errorData = new LinkedHashMap<>();
        errorData.put("code", code);
        if (details != null) {
            errorData.put("details", details);
        }

        return Response.status(statusCode)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiResponse<>(false, message, errorData))
                .build();
    }

    private String resolveMessage(Exception exception, String fallback) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return fallback;
        }
        return exception.getMessage();
    }
}
