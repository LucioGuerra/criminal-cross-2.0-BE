package org.athlium.bookings.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.bookings.application.usecase.CancelBookingUseCase;
import org.athlium.bookings.application.usecase.CreateBookingUseCase;
import org.athlium.bookings.application.usecase.GetBookingsUseCase;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.presentation.dto.BookingPageResponse;
import org.athlium.bookings.presentation.dto.CreateBookingRequest;
import org.athlium.bookings.presentation.mapper.BookingDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    @Inject
    CreateBookingUseCase createBookingUseCase;

    @Inject
    CancelBookingUseCase cancelBookingUseCase;

    @Inject
    GetBookingsUseCase getBookingsUseCase;

    @Inject
    BookingDtoMapper bookingDtoMapper;

    @POST
    @Path("/sessions/{sessionId}/bookings")
    public Response createBooking(
            @PathParam("sessionId") Long sessionId,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            CreateBookingRequest request
    ) {
        try {
            var booking = createBookingUseCase.execute(
                    sessionId,
                    request != null ? request.getUserId() : null,
                    idempotencyKey
            );
            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Booking created", bookingDtoMapper.toResponse(booking)))
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @POST
    @Path("/bookings/{bookingId}/cancel")
    public Response cancelBooking(
            @PathParam("bookingId") Long bookingId,
            @HeaderParam("Idempotency-Key") String idempotencyKey
    ) {
        try {
            var result = cancelBookingUseCase.execute(bookingId, idempotencyKey);
            return Response.ok(ApiResponse.success("Booking cancelled", bookingDtoMapper.toCancelResponse(result))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ApiResponse.error(e.getMessage())).build();
        }
    }

    @GET
    @Path("/bookings")
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
            Instant fromInstant = parseInstant(from, "from");
            Instant toInstant = parseInstant(to, "to");

            var result = getBookingsUseCase.execute(
                    sessionId,
                    userId,
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
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ApiResponse.error(e.getMessage())).build();
        }
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
