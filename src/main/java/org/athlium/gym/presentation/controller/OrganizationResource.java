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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.CreateOrganizationUseCase;
import org.athlium.gym.application.usecase.DeleteOrganizationUseCase;
import org.athlium.gym.application.usecase.GetOrganizationUseCase;
import org.athlium.gym.application.usecase.GetOrganizationsUseCase;
import org.athlium.gym.application.usecase.UpdateOrganizationUseCase;
import org.athlium.gym.presentation.dto.OrganizationInput;
import org.athlium.gym.presentation.mapper.OrganizationDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.EntityNotFoundException;

@Path("/api/organizations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrganizationResource {

    @Inject
    CreateOrganizationUseCase createOrganizationUseCase;

    @Inject
    GetOrganizationUseCase getOrganizationUseCase;

    @Inject
    GetOrganizationsUseCase getOrganizationsUseCase;

    @Inject
    UpdateOrganizationUseCase updateOrganizationUseCase;

    @Inject
    DeleteOrganizationUseCase deleteOrganizationUseCase;

    @Inject
    OrganizationDtoMapper mapper;

    @GET
    public Response getOrganizations() {
        var response = getOrganizationsUseCase.execute().stream().map(mapper::toResponse).toList();
        return Response.ok(ApiResponse.success("Organizations retrieved", response)).build();
    }

    @GET
    @Path("/{id}")
    public Response getOrganization(@PathParam("id") Long id) {
        try {
            var organization = getOrganizationUseCase.execute(id);
            return Response.ok(ApiResponse.success("Organization found", mapper.toResponse(organization))).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @POST
    public Response createOrganization(OrganizationInput input) {
        var created = createOrganizationUseCase.execute(mapper.toDomain(input));
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Organization created", mapper.toResponse(created)))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateOrganization(@PathParam("id") Long id, OrganizationInput input) {
        try {
            var updated = updateOrganizationUseCase.execute(id, mapper.toDomain(input));
            return Response.ok(ApiResponse.success("Organization updated", mapper.toResponse(updated))).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrganization(@PathParam("id") Long id) {
        try {
            deleteOrganizationUseCase.execute(id);
            return Response.ok(ApiResponse.success("Organization deleted", null)).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }
}
