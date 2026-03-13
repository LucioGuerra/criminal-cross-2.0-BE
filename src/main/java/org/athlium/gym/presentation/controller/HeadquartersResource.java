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
import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.gym.application.usecase.CreateHeadquartersUseCase;
import org.athlium.gym.application.usecase.DeleteHeadquartersUseCase;
import org.athlium.gym.application.usecase.GetActivitiesUseCase;
import org.athlium.gym.application.usecase.GetAllHeadquartersUseCase;
import org.athlium.gym.application.usecase.GetHeadquartersByOrganizationUseCase;
import org.athlium.gym.application.usecase.GetHeadquartersUseCase;
import org.athlium.gym.application.usecase.GetOrganizationUseCase;
import org.athlium.gym.application.usecase.GetSessionsUseCase;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.application.usecase.UpdateHeadquartersUseCase;
import org.athlium.gym.presentation.dto.HeadquartersInput;
import org.athlium.gym.presentation.dto.HeadquartersResponse;
import org.athlium.gym.presentation.mapper.ActivityDtoMapper;
import org.athlium.gym.presentation.mapper.HeadquartersDtoMapper;
import org.athlium.gym.presentation.mapper.SessionDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/api/headquarters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated(roles = {"SUPERADMIN", "ORG_OWNER", "ORG_ADMIN", "PROFESSOR"})
public class HeadquartersResource {

    @Inject
    CreateHeadquartersUseCase createHeadquartersUseCase;

    @Inject
    GetHeadquartersUseCase getHeadquartersUseCase;

    @Inject
    GetAllHeadquartersUseCase getAllHeadquartersUseCase;

    @Inject
    GetHeadquartersByOrganizationUseCase getHeadquartersByOrganizationUseCase;

    @Inject
    GetOrganizationUseCase getOrganizationUseCase;

    @Inject
    GetActivitiesUseCase getActivitiesUseCase;

    @Inject
    GetSessionsUseCase getSessionsUseCase;

    @Inject
    UpdateHeadquartersUseCase updateHeadquartersUseCase;

    @Inject
    DeleteHeadquartersUseCase deleteHeadquartersUseCase;

    @Inject
    HeadquartersDtoMapper mapper;

    @Inject
    ActivityDtoMapper activityDtoMapper;

    @Inject
    SessionDtoMapper sessionDtoMapper;

    @GET
    @Authenticated
    public Response getHeadquarters(@QueryParam("organizationId") Long organizationId) {
        var result = organizationId == null
                ? getAllHeadquartersUseCase.execute()
                : getHeadquartersByOrganizationUseCase.execute(organizationId);
        var response = result.stream().map(this::toResponseWithActivitiesAndSessions).toList();
        return Response.ok(ApiResponse.success("Headquarters retrieved", response)).build();
    }

    @GET
    @Path("/{id}")
    @Authenticated
    public Response getHeadquarter(@PathParam("id") Long id) {
        try {
            var headquarters = getHeadquartersUseCase.execute(id);
            return Response.ok(ApiResponse.success("Headquarters found", toResponseWithActivitiesAndSessions(headquarters)))
                    .build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    private HeadquartersResponse toResponseWithActivitiesAndSessions(Headquarters headquarters) {
        HeadquartersResponse response = mapper.toResponse(headquarters);

        // Fetch and set organization details
        try {
            var organization = getOrganizationUseCase.execute(headquarters.getOrganizationId());
            response.setOrganization(mapper.toOrganizationResponse(organization));
        } catch (Exception e) {
            // If organization not found, set organizationId only
            // The response already has organizationId from the mapper
        }

        List<org.athlium.gym.domain.model.Activity> activities =
                getActivitiesUseCase.executeAllByHeadquarter(headquarters.getId(), null);
        List<org.athlium.gym.domain.model.SessionInstance> sessions = loadAllSessionsByHeadquarters(headquarters.getId());

        Map<Long, List<org.athlium.gym.presentation.dto.SessionResponse>> sessionsByActivity = sessions.stream()
                .filter(session -> session.getActivityId() != null)
                .collect(Collectors.groupingBy(
                        org.athlium.gym.domain.model.SessionInstance::getActivityId,
                        Collectors.mapping(sessionDtoMapper::toResponse, Collectors.toList())
                ));

        var activityResponses = activityDtoMapper.toResponseList(activities);
        for (var activityResponse : activityResponses) {
            activityResponse.setSessions(sessionsByActivity.getOrDefault(activityResponse.getId(), List.of()));
        }

        response.setActivities(activityResponses);
        return response;
    }

    private List<org.athlium.gym.domain.model.SessionInstance> loadAllSessionsByHeadquarters(Long headquartersId) {
        final int pageSize = 100;
        int page = 1;
        List<org.athlium.gym.domain.model.SessionInstance> sessions = new ArrayList<>();

        while (true) {
            var pageResponse = getSessionsUseCase.execute(
                    null,
                    headquartersId,
                    null,
                    null,
                    null,
                    null,
                    page,
                    pageSize,
                    "startsAt:asc"
            );

            sessions.addAll(pageResponse.getContent());
            if (pageResponse.getContent().size() < pageSize) {
                return sessions;
            }

            page++;
        }
    }

    @POST
    @Authenticated(roles = {"SUPERADMIN", "ORG_OWNER", "ORG_ADMIN"})
    public Response createHeadquarters(HeadquartersInput input) {
        var created = createHeadquartersUseCase.execute(mapper.toDomain(input));
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Headquarters created", mapper.toResponse(created)))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Authenticated(roles = {"SUPERADMIN", "ORG_OWNER", "ORG_ADMIN"})
    public Response updateHeadquarters(@PathParam("id") Long id, HeadquartersInput input) {
        try {
            var updated = updateHeadquartersUseCase.execute(id, mapper.toDomain(input));
            return Response.ok(ApiResponse.success("Headquarters updated", mapper.toResponse(updated))).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Authenticated(roles = {"SUPERADMIN", "ORG_OWNER", "ORG_ADMIN"})
    public Response deleteHeadquarters(@PathParam("id") Long id) {
        try {
            deleteHeadquartersUseCase.execute(id);
            return Response.ok(ApiResponse.success("Headquarters deleted", null)).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }
}
