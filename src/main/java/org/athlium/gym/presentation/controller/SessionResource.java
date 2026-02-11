package org.athlium.gym.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.GetSessionByIdUseCase;
import org.athlium.gym.application.usecase.GetSessionsUseCase;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.presentation.dto.SessionPageResponse;
import org.athlium.gym.presentation.mapper.SessionDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {

    @Inject
    GetSessionsUseCase getSessionsUseCase;

    @Inject
    GetSessionByIdUseCase getSessionByIdUseCase;

    @Inject
    SessionDtoMapper sessionDtoMapper;

    @GET
    public Response getSessions(
            @QueryParam("organizationId") Long organizationId,
            @QueryParam("headquartersId") Long headquartersId,
            @QueryParam("gymId") Long gymId,
            @QueryParam("branchId") Long branchId,
            @QueryParam("activityId") Long activityId,
            @QueryParam("status") SessionStatus status,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("20") @QueryParam("limit") int limit,
            @DefaultValue("startsAt:asc") @QueryParam("sort") String sort
    ) {
        try {
            Long effectiveOrganizationId = organizationId != null ? organizationId : gymId;
            Long effectiveHeadquartersId = headquartersId != null ? headquartersId : branchId;
            Instant fromInstant = parseInstant(from, "from");
            Instant toInstant = parseInstant(to, "to");

            var result = getSessionsUseCase.execute(
                    effectiveOrganizationId,
                    effectiveHeadquartersId,
                    activityId,
                    status,
                    fromInstant,
                    toInstant,
                    page,
                    limit,
                    sort
            );

            var response = new SessionPageResponse(
                    sessionDtoMapper.toResponseList(result.getContent()),
                    result.getPage() + 1,
                    result.getSize(),
                    result.getTotalElements()
            );

            return Response.ok(ApiResponse.success("Sessions retrieved successfully", response)).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    private Instant parseInstant(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(fieldName + " must be a valid ISO-8601 instant");
        }
    }

    @GET
    @Path("/{id}")
    public Response getSessionById(@PathParam("id") Long id) {
        try {
            var session = getSessionByIdUseCase.execute(id);
            return Response.ok(ApiResponse.success("Session found", sessionDtoMapper.toResponse(session))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }
}
