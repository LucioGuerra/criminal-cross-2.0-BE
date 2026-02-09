package org.athlium.bookings.presentation.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.bookings.application.usecase.CreateBookingUseCase;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.presentation.dto.CreateBookingRequest;
import org.athlium.bookings.presentation.mapper.BookingDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionBookingResourceUnitTest {

    private SessionBookingResource resource;
    private StubCreateBookingUseCase createUseCase;

    @BeforeEach
    void setUp() {
        resource = new SessionBookingResource();
        createUseCase = new StubCreateBookingUseCase();

        resource.createBookingUseCase = createUseCase;
        resource.bookingDtoMapper = new BookingDtoMapper();
    }

    @Test
    void shouldCreateBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setSessionId(10L);
        booking.setUserId(100L);
        booking.setStatus(BookingStatus.CONFIRMED);
        createUseCase.response = booking;

        CreateBookingRequest request = new CreateBookingRequest();
        request.setUserId(100L);

        Response response = resource.createBooking(10L, "key-1", request);

        assertEquals(201, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Booking created", body.getMessage());
    }

    private static class StubCreateBookingUseCase extends CreateBookingUseCase {
        Booking response;

        @Override
        public Booking execute(Long sessionId, Long userId, String requestId) {
            return response;
        }
    }
}
