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
import org.athlium.gym.application.usecase.CreateHeadquartersUseCase;
import org.athlium.gym.application.usecase.DeleteHeadquartersUseCase;
import org.athlium.gym.application.usecase.GetAllHeadquartersUseCase;
import org.athlium.gym.application.usecase.GetHeadquartersByOrganizationUseCase;
import org.athlium.gym.application.usecase.GetHeadquartersUseCase;
import org.athlium.gym.application.usecase.UpdateHeadquartersUseCase;
import org.athlium.gym.presentation.dto.HeadquartersInput;
import org.athlium.gym.presentation.mapper.HeadquartersDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.EntityNotFoundException;

@Path("/api/headquarters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
    UpdateHeadquartersUseCase updateHeadquartersUseCase;

    @Inject
    DeleteHeadquartersUseCase deleteHeadquartersUseCase;

    @Inject
    HeadquartersDtoMapper mapper;

    @GET
    public Response getHeadquarters(@QueryParam("organizationId") Long organizationId) {
        var result = organizationId == null
                ? getAllHeadquartersUseCase.execute()
                : getHeadquartersByOrganizationUseCase.execute(organizationId);
        var response = result.stream().map(mapper::toResponse).toList();
        return Response.ok(ApiResponse.success("Headquarters retrieved", response)).build();
    }

    @GET
    @Path("/{id}")
    public Response getHeadquarter(@PathParam("id") Long id) {
        try {
            var headquarters = getHeadquartersUseCase.execute(id);
            return Response.ok(ApiResponse.success("Headquarters found", mapper.toResponse(headquarters))).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @POST
    public Response createHeadquarters(HeadquartersInput input) {
        var created = createHeadquartersUseCase.execute(mapper.toDomain(input));
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Headquarters created", mapper.toResponse(created)))
                .build();
    }

    @PUT
    @Path("/{id}")
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
    public Response deleteHeadquarters(@PathParam("id") Long id) {
        try {
            deleteHeadquartersUseCase.execute(id);
            return Response.ok(ApiResponse.success("Headquarters deleted", null)).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }
}
