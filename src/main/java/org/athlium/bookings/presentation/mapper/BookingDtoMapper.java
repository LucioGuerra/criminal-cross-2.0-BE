package org.athlium.bookings.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.bookings.application.usecase.CancelBookingUseCase;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.presentation.dto.BookingResponse;
import org.athlium.bookings.presentation.dto.CancelBookingResponse;

import java.util.List;

@ApplicationScoped
public class BookingDtoMapper {

    public BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setSessionId(booking.getSessionId());
        response.setUserId(booking.getUserId());
        response.setStatus(booking.getStatus());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        response.setCancelledAt(booking.getCancelledAt());
        return response;
    }

    public List<BookingResponse> toResponseList(List<Booking> bookings) {
        return bookings.stream().map(this::toResponse).toList();
    }

    public CancelBookingResponse toCancelResponse(CancelBookingUseCase.CancelBookingResult result) {
        CancelBookingResponse response = new CancelBookingResponse();
        response.setCancelledBooking(toResponse(result.cancelledBooking()));
        if (result.promotedBooking() != null) {
            response.setPromotedBooking(toResponse(result.promotedBooking()));
        }
        return response;
    }
}
