package org.athlium.gym.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.ResolveSessionConfigurationUseCase;
import org.athlium.gym.application.usecase.UpsertActivityConfigUseCase;
import org.athlium.gym.application.usecase.UpsertHeadquartersConfigUseCase;
import org.athlium.gym.application.usecase.UpsertOrganizationConfigUseCase;
import org.athlium.gym.application.usecase.UpsertSessionConfigUseCase;
import org.athlium.gym.presentation.dto.SessionConfigurationRequest;
import org.athlium.gym.presentation.mapper.SessionConfigurationDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;

@Path("/api/gym/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionConfigurationResource {

    @Inject
    UpsertOrganizationConfigUseCase upsertOrganizationConfigUseCase;

    @Inject
    UpsertHeadquartersConfigUseCase upsertHeadquartersConfigUseCase;

    @Inject
    UpsertActivityConfigUseCase upsertActivityConfigUseCase;

    @Inject
    UpsertSessionConfigUseCase upsertSessionConfigUseCase;

    @Inject
    ResolveSessionConfigurationUseCase resolveSessionConfigurationUseCase;

    @Inject
    SessionConfigurationDtoMapper mapper;

    @PUT
    @Path("/organizations/{organizationId}")
    public Response upsertOrganizationConfig(
            @PathParam("organizationId") Long organizationId,
            SessionConfigurationRequest request
    ) {
        try {
            var saved = upsertOrganizationConfigUseCase.execute(organizationId, mapper.toDomain(request));
            return Response.ok(ApiResponse.success("Organization config updated", mapper.toResponse(saved))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @PUT
    @Path("/headquarters/{headquartersId}")
    public Response upsertHeadquartersConfig(
            @PathParam("headquartersId") Long headquartersId,
            SessionConfigurationRequest request
    ) {
        try {
            var saved = upsertHeadquartersConfigUseCase.execute(headquartersId, mapper.toDomain(request));
            return Response.ok(ApiResponse.success("Headquarters config updated", mapper.toResponse(saved))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @PUT
    @Path("/activities/{activityId}")
    public Response upsertActivityConfig(
            @PathParam("activityId") Long activityId,
            SessionConfigurationRequest request
    ) {
        try {
            var saved = upsertActivityConfigUseCase.execute(activityId, mapper.toDomain(request));
            return Response.ok(ApiResponse.success("Activity config updated", mapper.toResponse(saved))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @PUT
    @Path("/sessions/{sessionId}")
    public Response upsertSessionConfig(
            @PathParam("sessionId") Long sessionId,
            SessionConfigurationRequest request
    ) {
        try {
            var saved = upsertSessionConfigUseCase.execute(sessionId, mapper.toDomain(request));
            return Response.ok(ApiResponse.success("Session override updated", mapper.toResponse(saved))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @GET
    @Path("/effective")
    public Response getEffectiveConfig(
            @QueryParam("organizationId") Long organizationId,
            @QueryParam("headquartersId") Long headquartersId,
            @QueryParam("activityId") Long activityId,
            @QueryParam("sessionId") Long sessionId
    ) {
        try {
            var effective = resolveSessionConfigurationUseCase.execute(organizationId, headquartersId, activityId, sessionId);
            return Response.ok(ApiResponse.success("Effective config resolved", mapper.toResponse(effective))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }
}
