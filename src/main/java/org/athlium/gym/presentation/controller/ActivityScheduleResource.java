package org.athlium.gym.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.CreateActivityScheduleUseCase;
import org.athlium.gym.application.usecase.GetActivitySchedulesUseCase;
import org.athlium.gym.application.usecase.GenerateNextWeekSessionsUseCase;
import org.athlium.gym.presentation.dto.ActivityScheduleRequest;
import org.athlium.gym.presentation.mapper.ActivityScheduleDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;

@Path("/api/activity-schedules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ActivityScheduleResource {

    @Inject
    CreateActivityScheduleUseCase createActivityScheduleUseCase;

    @Inject
    GetActivitySchedulesUseCase getActivitySchedulesUseCase;

    @Inject
    GenerateNextWeekSessionsUseCase generateNextWeekSessionsUseCase;

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

    @POST
    @Path("/generate-next-week")
    public Response generateNextWeek(@DefaultValue("false") @QueryParam("dryRun") boolean dryRun) {
        if (dryRun) {
            return Response.ok(ApiResponse.success("Dry run is not supported yet", null)).build();
        }

        var result = generateNextWeekSessionsUseCase.execute();
        return Response.ok(ApiResponse.success("Sessions generated", result)).build();
    }
}
