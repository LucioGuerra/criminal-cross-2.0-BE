package org.athlium.clients.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.PATCH;
import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.clients.application.usecase.CreateClientPackageUseCase;
import org.athlium.clients.application.usecase.GetActiveClientPackageUseCase;
import org.athlium.clients.application.usecase.GetClientPackagesUseCase;
import org.athlium.clients.application.usecase.UpdateActiveClientPackageUseCase;
import org.athlium.clients.presentation.dto.ClientPackageUpsertRequest;
import org.athlium.clients.presentation.mapper.ClientPackageDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@Path("/api/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientPackageResource {

    @Inject
    CreateClientPackageUseCase createClientPackageUseCase;

    @Inject
    UpdateActiveClientPackageUseCase updateActiveClientPackageUseCase;

    @Inject
    GetActiveClientPackageUseCase getActiveClientPackageUseCase;

    @Inject
    GetClientPackagesUseCase getClientPackagesUseCase;

    @Inject
    ClientPackageDtoMapper dtoMapper;

    @POST
    @Path("/{userId}/packages")
    public Response createPackage(@PathParam("userId") Long userId, ClientPackageUpsertRequest request) {
        try {
            var created = createClientPackageUseCase.execute(
                    userId,
                    request != null ? request.getPaymentId() : null,
                    dtoMapper.toCredits(request)
            );
            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Client package created", dtoMapper.toResponse(created)))
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @PATCH
    @Path("/{userId}/packages/{packageId}")
    public Response updateActivePackage(
            @PathParam("userId") Long userId,
            @PathParam("packageId") Long packageId,
            ClientPackageUpsertRequest request
    ) {
        try {
            var updated = updateActiveClientPackageUseCase.execute(userId, packageId, dtoMapper.toCredits(request));
            return Response.ok(ApiResponse.success("Client package updated", dtoMapper.toResponse(updated))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @GET
    @Path("/{userId}/packages/active")
    public Response getActivePackage(@PathParam("userId") Long userId) {
        try {
            var active = getActiveClientPackageUseCase.execute(userId);
            return Response.ok(ApiResponse.success("Active packages retrieved", dtoMapper.toResponseList(active))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @GET
    @Path("/{userId}/packages")
    public Response getPackages(@PathParam("userId") Long userId) {
        try {
            var packages = getClientPackagesUseCase.execute(userId);
            return Response.ok(ApiResponse.success("Client packages retrieved", dtoMapper.toResponseList(packages))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }
}
