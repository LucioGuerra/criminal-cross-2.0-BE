package org.athlium.users.infrastructure.controller;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.auth.infrastructure.security.SecurityContext;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.DomainException;
import org.athlium.users.application.usecase.*;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.infrastructure.dto.CreateUserRequestDto;
import org.athlium.users.infrastructure.dto.UpdateUserRequestDto;
import org.athlium.users.infrastructure.dto.UpdateRolesRequestDto;
import org.athlium.users.infrastructure.mapper.UserDtoMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.EnumSet;
import java.util.Set;

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
    UpdateUserUseCase updateUserUseCase;

    @Inject
    AssignUserToHeadquartersUseCase assignUserToHeadquartersUseCase;

    @Inject
    RemoveUserFromHeadquartersUseCase removeUserFromHeadquartersUseCase;

    @Inject
    UserDtoMapper userDtoMapper;

    @Inject
    SecurityContext securityContext;

    @ConfigProperty(name = "auth.dev-bypass.enabled", defaultValue = "false")
    boolean authBypassEnabled;

    @POST
    @Transactional
    @Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})
    public Response createUser(@Valid CreateUserRequestDto request) {
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
    @Authenticated
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
    @Path("/firebase/{uid}")
    @Transactional
    @Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})
    public Response updateUser(@PathParam("uid") String firebaseUid, @Valid UpdateUserRequestDto request) {
        try {
            AuthenticatedUser authUser = securityContext.requireCurrentUser();

            User currentUser = getCurrentUserForAdministrativeActions(authUser);

            var user = updateUserUseCase.execute(
                    firebaseUid,
                    request.getEmail(),
                    request.getName(),
                    request.getLastName(),
                    request.getActive(),
                    currentUser);
            var response = userDtoMapper.toResponseDto(user);
            return Response.ok(ApiResponse.success("User updated successfully", response)).build();
        } catch (DomainException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error("Authentication is required"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/firebase/{uid}/roles")
    @Transactional
    @Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN"})
    public Response updateUserRoles(@PathParam("uid") String firebaseUid, @Valid UpdateRolesRequestDto request) {
        try {
            AuthenticatedUser authUser = securityContext.requireCurrentUser();

            User currentUser = getCurrentUserForAdministrativeActions(authUser);

            var user = updateUserRolesUseCase.execute(firebaseUid, request.getRoles(), currentUser);
            var response = userDtoMapper.toResponseDto(user);
            return Response.ok(ApiResponse.success("User roles updated successfully", response)).build();
        } catch (DomainException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error("Authentication is required"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/firebase/{uid}/headquarters/{headquartersId}")
    @Transactional
    @Authenticated
    public Response assignUserToHeadquarters(@PathParam("uid") String firebaseUid,
            @PathParam("headquartersId") Long headquartersId) {
        try {
            AuthenticatedUser authUser = securityContext.requireCurrentUser();
            User currentUser = getCurrentUserForAdministrativeActions(authUser);

            var user = assignUserToHeadquartersUseCase.execute(firebaseUid, headquartersId, currentUser);
            var response = userDtoMapper.toResponseDto(user);
            return Response.ok(ApiResponse.success("User assigned to headquarters successfully", response)).build();
        } catch (DomainException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error("Authentication is required"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/firebase/{uid}/headquarters/{headquartersId}")
    @Transactional
    @Authenticated
    public Response removeUserFromHeadquarters(@PathParam("uid") String firebaseUid,
            @PathParam("headquartersId") Long headquartersId) {
        try {
            AuthenticatedUser authUser = securityContext.requireCurrentUser();
            User currentUser = getCurrentUserForAdministrativeActions(authUser);

            var user = removeUserFromHeadquartersUseCase.execute(firebaseUid, headquartersId, currentUser);
            var response = userDtoMapper.toResponseDto(user);
            return Response.ok(ApiResponse.success("User removed from headquarters successfully", response)).build();
        } catch (DomainException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error("Authentication is required"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/sync")
    public Response syncWithFirebase(@Valid CreateUserRequestDto request) {
        return Response.status(Response.Status.GONE)
                .entity(ApiResponse.error("Endpoint deprecated. Use /api/auth/register for user creation"))
                .build();
    }

    private User getCurrentUserForAdministrativeActions(AuthenticatedUser authUser) {
        if (authBypassEnabled) {
            return User.builder()
                    .firebaseUid(authUser.getFirebaseUid())
                    .email(authUser.getEmail())
                    .name(authUser.getName())
                    .roles(EnumSet.of(Role.SUPERADMIN))
                    .headquartersIds(Set.of())
                    .active(true)
                    .build();
        }

        return getUserByUidUseCase.execute(authUser.getFirebaseUid())
                .orElseThrow(() -> new DomainException("Current user not found"));
    }
}
