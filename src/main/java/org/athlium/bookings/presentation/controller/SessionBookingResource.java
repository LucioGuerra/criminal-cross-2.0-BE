package org.athlium.bookings.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.auth.infrastructure.security.SecurityContext;
import org.athlium.bookings.application.usecase.CreateBookingUseCase;
import org.athlium.bookings.presentation.dto.CreateBookingRequest;
import org.athlium.bookings.presentation.mapper.BookingDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.users.domain.model.Role;

@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated(roles = {"CLIENT", "PROFESSOR", "ORG_ADMIN", "ORG_OWNER", "SUPERADMIN"})
public class SessionBookingResource {

    @Inject
    CreateBookingUseCase createBookingUseCase;

    @Inject
    BookingDtoMapper bookingDtoMapper;

    @Inject
    SecurityContext securityContext;

    @POST
    @Path("/{sessionId}/bookings")
    public Response createBooking(
            @PathParam("sessionId") Long sessionId,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            CreateBookingRequest request
    ) {
        try {
            AuthenticatedUser currentUser = securityContext.requireCurrentUser();
            Long targetUserId = resolveTargetUserId(currentUser, request != null ? request.getUserId() : null);

            var booking = createBookingUseCase.execute(
                    sessionId,
                    targetUserId,
                    idempotencyKey
            );
            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Booking created", bookingDtoMapper.toResponse(booking)))
                    .build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ApiResponse.error(e.getMessage())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ApiResponse.error("Authentication is required")).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    private Long resolveTargetUserId(AuthenticatedUser currentUser, Long requestedUserId) {
        Long currentUserId = requireCurrentUserId(currentUser);
        Long targetUserId = requestedUserId != null ? requestedUserId : currentUserId;

        if (isElevated(currentUser) || targetUserId.equals(currentUserId)) {
            return targetUserId;
        }

        throw new ForbiddenException("You can only create bookings for your own user");
    }

    private Long requireCurrentUserId(AuthenticatedUser currentUser) {
        if (currentUser.getUserId() == null || currentUser.getUserId() <= 0) {
            throw new ForbiddenException("Authenticated user is not linked to a valid local user");
        }
        return currentUser.getUserId();
    }

    private boolean isElevated(AuthenticatedUser currentUser) {
        return currentUser.hasAnyRole(Role.PROFESSOR, Role.ORG_OWNER, Role.ORG_ADMIN, Role.SUPERADMIN);
    }
}
