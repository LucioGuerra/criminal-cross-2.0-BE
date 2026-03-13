package org.athlium.bookings.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.auth.infrastructure.security.SecurityContext;
import org.athlium.bookings.application.usecase.CancelBookingUseCase;
import org.athlium.bookings.application.usecase.GetBookingsUseCase;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.bookings.presentation.dto.BookingPageResponse;
import org.athlium.bookings.presentation.mapper.BookingDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.users.domain.model.Role;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    @Inject
    CancelBookingUseCase cancelBookingUseCase;

    @Inject
    GetBookingsUseCase getBookingsUseCase;

    @Inject
    BookingDtoMapper bookingDtoMapper;

    @Inject
    SecurityContext securityContext;

    @Inject
    BookingRepository bookingRepository;

    @POST
    @Path("/bookings/{bookingId}/cancel")
    @Authenticated(roles = {"CLIENT", "PROFESSOR", "ORG_ADMIN", "ORG_OWNER", "SUPERADMIN"})
    public Response cancelBooking(
            @PathParam("bookingId") Long bookingId,
            @HeaderParam("Idempotency-Key") String idempotencyKey
    ) {
        try {
            AuthenticatedUser currentUser = securityContext.requireCurrentUser();
            enforceCanManageBooking(currentUser, bookingId);

            var result = cancelBookingUseCase.execute(bookingId, idempotencyKey);
            return Response.ok(ApiResponse.success("Booking cancelled", bookingDtoMapper.toCancelResponse(result))).build();
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

    @GET
    @Path("/bookings")
    @Authenticated(roles = {"CLIENT", "SUPERADMIN", "ORG_OWNER", "ORG_ADMIN", "PROFESSOR"})
    public Response getBookings(
            @QueryParam("sessionId") Long sessionId,
            @QueryParam("userId") Long userId,
            @QueryParam("status") BookingStatus status,
            @QueryParam("branchId") Long branchId,
            @QueryParam("activityId") Long activityId,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("20") @QueryParam("limit") int limit,
            @DefaultValue("createdAt:desc") @QueryParam("sort") String sort
    ) {
        try {
            AuthenticatedUser currentUser = securityContext.requireCurrentUser();
            Long effectiveUserId = resolveRequestedUserId(currentUser, userId, "You can only list your own bookings");

            Instant fromInstant = parseInstant(from, "from");
            Instant toInstant = parseInstant(to, "to");

            var result = getBookingsUseCase.execute(
                    sessionId,
                    effectiveUserId,
                    status,
                    branchId,
                    activityId,
                    fromInstant,
                    toInstant,
                    page,
                    limit,
                    sort
            );

            var response = new BookingPageResponse(
                    bookingDtoMapper.toResponseList(result.getContent()),
                    result.getPage() + 1,
                    result.getSize(),
                    result.getTotalElements()
            );

            return Response.ok(ApiResponse.success("Bookings retrieved", response)).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(ApiResponse.error(e.getMessage())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ApiResponse.error("Authentication is required")).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    private void enforceCanManageBooking(AuthenticatedUser currentUser, Long bookingId) {
        if (isElevated(currentUser)) {
            return;
        }
        if (bookingId == null || bookingId <= 0) {
            return;
        }

        Long currentUserId = requireCurrentUserId(currentUser);
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (!booking.getUserId().equals(currentUserId)) {
                throw new ForbiddenException("You can only cancel your own bookings");
            }
        });
    }

    private Long resolveRequestedUserId(AuthenticatedUser currentUser, Long requestedUserId, String forbiddenMessage) {
        Long currentUserId = requireCurrentUserId(currentUser);
        Long effectiveUserId = requestedUserId != null ? requestedUserId : currentUserId;

        if (isElevated(currentUser) || effectiveUserId.equals(currentUserId)) {
            return effectiveUserId;
        }

        throw new ForbiddenException(forbiddenMessage);
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

    private Instant parseInstant(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(fieldName + " must be a valid ISO-8601 instant");
        }
    }
}
