package org.athlium.gym.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.auth.infrastructure.security.PublicEndpoint;
import org.athlium.gym.application.usecase.CreateActivityScheduleUseCase;
import org.athlium.gym.application.usecase.DeleteActivityScheduleUseCase;
import org.athlium.gym.application.usecase.GetActivitySchedulesUseCase;
import org.athlium.gym.application.usecase.GenerateNextWeekSessionsUseCase;
import org.athlium.gym.application.usecase.UpdateActivityScheduleUseCase;
import org.athlium.gym.presentation.dto.ActivityScheduleRequest;
import org.athlium.gym.presentation.mapper.ActivityScheduleDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@Path("/api/activity-schedules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated(roles = {"SUPERADMIN", "ORG_OWNER", "ORG_ADMIN", "PROFESSOR"})
public class ActivityScheduleResource {

    @Inject
    CreateActivityScheduleUseCase createActivityScheduleUseCase;

    @Inject
    GetActivitySchedulesUseCase getActivitySchedulesUseCase;

    @Inject
    GenerateNextWeekSessionsUseCase generateNextWeekSessionsUseCase;

    @Inject
    DeleteActivityScheduleUseCase deleteActivityScheduleUseCase;

    @Inject
    UpdateActivityScheduleUseCase updateActivityScheduleUseCase;

    @Inject
    ActivityScheduleDtoMapper mapper;

    @POST
    public Response create(ActivityScheduleRequest request) {
        try {
            var created = createActivityScheduleUseCase.execute(mapper.toDomain(request));
            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Schedule created", mapper.toResponse(created)))
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @GET
    public Response list(@QueryParam("headquartersId") Long headquartersId) {
        var schedules = getActivitySchedulesUseCase.execute(headquartersId);
        return Response.ok(ApiResponse.success("Schedules retrieved", mapper.toResponseList(schedules))).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, ActivityScheduleRequest request) {
        try {
            var updated = updateActivityScheduleUseCase.execute(id, mapper.toDomain(request));
            return Response.ok(ApiResponse.success("Schedule updated", mapper.toResponse(updated))).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage())).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Unexpected error")).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            var deleted = deleteActivityScheduleUseCase.execute(id);
            return Response.ok(ApiResponse.success("Schedule deleted", mapper.toResponse(deleted))).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage())).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Unexpected error")).build();
        }
    }

    @POST
    @PublicEndpoint
    @Path("/generate-next-week")
    public Response generateNextWeek(@DefaultValue("false") @QueryParam("dryRun") boolean dryRun) {
        if (dryRun) {
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                    .entity(ApiResponse.error("Dry run is not supported yet"))
                    .build();
        }

        var result = generateNextWeekSessionsUseCase.execute();
        return Response.ok(ApiResponse.success("Sessions generated", result)).build();
    }
}
