package org.athlium.bookings.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.bookings.application.usecase.CreateBookingUseCase;
import org.athlium.bookings.presentation.dto.CreateBookingRequest;
import org.athlium.bookings.presentation.mapper.BookingDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionBookingResource {

    @Inject
    CreateBookingUseCase createBookingUseCase;

    @Inject
    BookingDtoMapper bookingDtoMapper;

    @POST
    @Path("/{sessionId}/bookings")
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
}
