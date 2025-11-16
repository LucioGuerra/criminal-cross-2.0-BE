package org.athlium.users.infrastructure.controller;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.DomainException;
import org.athlium.users.application.usecase.*;
import org.athlium.users.infrastructure.dto.CreateUserRequestDto;
import org.athlium.users.infrastructure.dto.UpdateRolesRequestDto;
import org.athlium.users.infrastructure.mapper.UserDtoMapper;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    CreateUserUseCase createUserUseCase;

    @Inject
    GetUserByUidUseCase getUserByUidUseCase;

    @Inject
    UpdateUserRolesUseCase updateUserRolesUseCase;

    @Inject
    SyncUserWithFirebaseUseCase syncUserWithFirebaseUseCase;

    @Inject
    UserDtoMapper userDtoMapper;

    @POST
    @Transactional
    public Response createUser(CreateUserRequestDto request) {
        try {
            var user = createUserUseCase.execute(
                    request.getFirebaseUid(),
                    request.getEmail(),
                    request.getName(),
                    request.getLastName()
            );
            var response = userDtoMapper.toResponseDto(user);
            return Response.ok(ApiResponse.success("User created successfully", response)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/firebase/{uid}")
    public Response getUserByUid(@PathParam("uid") String firebaseUid) {
        var user = getUserByUidUseCase.execute(firebaseUid);
        if (user.isPresent()) {
            var response = userDtoMapper.toResponseDto(user.get());
            return Response.ok(ApiResponse.success("User found", response)).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiResponse.error("User not found"))
                .build();
    }

    @PUT
    @Path("/firebase/{uid}/roles")
    @Transactional
    public Response updateUserRoles(@PathParam("uid") String firebaseUid, UpdateRolesRequestDto request) {
        try {
            // TODO: Get current user from JWT token
            var currentUser = getUserByUidUseCase.execute("current-user-uid")
                    .orElseThrow(() -> new DomainException("Current user not found"));

            var user = updateUserRolesUseCase.execute(firebaseUid, request.getRoles(), currentUser);
            var response = userDtoMapper.toResponseDto(user);
            return Response.ok(ApiResponse.success("User roles updated successfully", response)).build();
        } catch (DomainException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/sync")
    @Transactional
    public Response syncWithFirebase(CreateUserRequestDto request) {
        try {
            var user = syncUserWithFirebaseUseCase.execute(
                    request.getFirebaseUid(),
                    request.getEmail(),
                    request.getName(),
                    request.getLastName()
            );
            var response = userDtoMapper.toResponseDto(user);
            return Response.ok(ApiResponse.success("User synchronized successfully", response)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }
}