package org.athlium.users.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.users.application.usecase.GetAllUsersUseCase;
import org.athlium.users.application.usecase.GetUserByIdUseCase;
import org.athlium.users.application.usecase.GetUsersByHqUseCase;
import org.athlium.users.application.usecase.GetUsersByOrgUseCase;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.infrastructure.dto.UserResponseDto;
import org.athlium.users.presentation.mapper.UserQueryDtoMapper;

import java.util.List;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN", "ORG_OWNER"})
public class UserQueryResource {

    @Inject
    GetUsersByHqUseCase getUsersByHqUseCase;

    @Inject
    GetUsersByOrgUseCase getUsersByOrgUseCase;

    @Inject
    GetAllUsersUseCase getAllUsersUseCase;

    @Inject
    GetUserByIdUseCase getUserByIdUseCase;

    @Inject
    UserQueryDtoMapper userQueryDtoMapper;

    @GET
    public Response getUsers(
            @QueryParam("headquartersId") Long headquartersId,
            @QueryParam("headquarterId") Long headquarterId,
            @QueryParam("headquartetsId") Long headquartetsId,
            @QueryParam("organizationId") Long organizationId,
            @QueryParam("status") String status,
            @QueryParam("search") String search,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("20") @QueryParam("size") int size,
            @DefaultValue("name:asc") @QueryParam("sort") String sort) {

        try {
            if (headquartersId != null && headquarterId != null && !headquartersId.equals(headquarterId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Query params headquartersId and headquarterId must match when both are provided"))
                        .build();
            }

            if (headquartersId != null && headquartetsId != null && !headquartersId.equals(headquartetsId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Query params headquartersId and headquartetsId must match when both are provided"))
                        .build();
            }

            if (headquarterId != null && headquartetsId != null && !headquarterId.equals(headquartetsId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Query params headquarterId and headquartetsId must match when both are provided"))
                        .build();
            }

            Long effectiveHeadquartersId = headquartersId != null ? headquartersId
                    : (headquarterId != null ? headquarterId : headquartetsId);

            if (effectiveHeadquartersId != null && organizationId != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Cannot filter by both headquartersId and organizationId"))
                        .build();
            }

            PageResponse<UserWithPackageStatus> usersPage;

            if (effectiveHeadquartersId != null) {
                usersPage = getUsersByHqUseCase.execute(effectiveHeadquartersId, status, search, page, size, sort);
            } else if (organizationId != null) {
                usersPage = getUsersByOrgUseCase.execute(organizationId, status, search, page, size, sort);
            } else {
                usersPage = getAllUsersUseCase.execute(status, search, page, size, sort);
            }

            List<UserResponseDto> mappedContent = userQueryDtoMapper.toResponseList(usersPage.getContent());
            PageResponse<UserResponseDto> mappedPage = new PageResponse<>(
                    mappedContent, usersPage.getPage(), usersPage.getSize(), usersPage.getTotalElements());

            return Response.ok(ApiResponse.success("Users retrieved", mappedPage)).build();

        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Unexpected error"))
                    .build();
        }
    }

    Response getUsers(
            Long headquartersId,
            Long headquarterId,
            Long organizationId,
            String status,
            String search,
            int page,
            int size,
            String sort) {
        return getUsers(headquartersId, headquarterId, null, organizationId, status, search, page, size, sort);
    }

    Response getUsers(
            Long headquartersId,
            Long organizationId,
            String status,
            String search,
            int page,
            int size,
            String sort) {
        return getUsers(headquartersId, null, null, organizationId, status, search, page, size, sort);
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        try {
            UserWithPackageStatus user = getUserByIdUseCase.execute(id);
            UserResponseDto response = userQueryDtoMapper.toResponse(user);
            return Response.ok(ApiResponse.success("User found", response)).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Unexpected error"))
                    .build();
        }
    }
}
