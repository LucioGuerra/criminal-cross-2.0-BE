package org.athlium.gym.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.CreateActivityUseCase;
import org.athlium.gym.application.usecase.DeleteActivityUseCase;
import org.athlium.gym.application.usecase.GetActivitiesUseCase;
import org.athlium.gym.application.usecase.GetActivityUseCase;
import org.athlium.gym.application.usecase.UpdateActivityUseCase;
import org.athlium.gym.presentation.dto.ActivityInput;
import org.athlium.gym.presentation.dto.ActivityUpdateInput;
import org.athlium.gym.presentation.mapper.ActivityDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@Path("/api/activities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ActivityResource {

    @Inject
    CreateActivityUseCase createActivityUseCase;

    @Inject
    GetActivityUseCase getActivityUseCase;

    @Inject
    UpdateActivityUseCase updateActivityUseCase;

    @Inject
    DeleteActivityUseCase deleteActivityUseCase;

    @Inject
    GetActivitiesUseCase getActivitiesUseCase;

    @Inject
    ActivityDtoMapper dtoMapper;

    @GET
    public Response getActivities(
            @QueryParam("hqId") Long hqId,
            @QueryParam("isActive") Boolean isActive,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @QueryParam("name") String name
    ) {
        try {
            if (name != null && !name.isBlank()) {
                int safePage = page == null ? 0 : page;
                int safeSize = size == null ? 20 : size;
                var pageResponse = getActivitiesUseCase.executeByName(name, hqId, safePage, safeSize);
                return Response.ok(ApiResponse.success("Activities retrieved", dtoMapper.toPageResponse(pageResponse))).build();
            }

            if (page != null || size != null) {
                int safePage = page == null ? 0 : page;
                int safeSize = size == null ? 20 : size;
                var pageResponse = getActivitiesUseCase.executeByHeadquarter(hqId, isActive, safePage, safeSize);
                return Response.ok(ApiResponse.success("Activities retrieved", dtoMapper.toPageResponse(pageResponse))).build();
            }

            var all = getActivitiesUseCase.executeAllByHeadquarter(hqId, isActive);
            return Response.ok(ApiResponse.success("Activities retrieved", dtoMapper.toResponseList(all))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getActivity(@PathParam("id") Long id) {
        try {
            var activity = getActivityUseCase.execute(id);
            return Response.ok(ApiResponse.success("Activity found", dtoMapper.toResponse(activity))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @POST
    public Response createActivity(ActivityInput input) {
        try {
            var created = createActivityUseCase.execute(input.getName(), input.getDescription(), input.getHqId());
            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Activity created", dtoMapper.toResponse(created)))
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateActivity(@PathParam("id") Long id, ActivityUpdateInput input) {
        try {
            ActivityUpdateInput normalized = input == null ? new ActivityUpdateInput() : input;
            normalized.setId(id);
            var updated = updateActivityUseCase.execute(dtoMapper.toDomain(normalized));
            return Response.ok(ApiResponse.success("Activity updated", dtoMapper.toResponse(updated))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteActivity(@PathParam("id") Long id) {
        try {
            deleteActivityUseCase.execute(id);
            return Response.ok(ApiResponse.success("Activity deleted", null)).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }
}
