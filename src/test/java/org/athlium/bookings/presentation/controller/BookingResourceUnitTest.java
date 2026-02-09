package org.athlium.bookings.presentation.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.bookings.application.usecase.CancelBookingUseCase;
import org.athlium.bookings.application.usecase.CreateBookingUseCase;
import org.athlium.bookings.application.usecase.GetBookingsUseCase;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.presentation.dto.CreateBookingRequest;
import org.athlium.bookings.presentation.mapper.BookingDtoMapper;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingResourceUnitTest {

    private BookingResource resource;
    private StubCreateBookingUseCase createUseCase;
    private StubCancelBookingUseCase cancelUseCase;
    private StubGetBookingsUseCase getUseCase;

    @BeforeEach
    void setUp() {
        resource = new BookingResource();
        createUseCase = new StubCreateBookingUseCase();
        cancelUseCase = new StubCancelBookingUseCase();
        getUseCase = new StubGetBookingsUseCase();

        resource.createBookingUseCase = createUseCase;
        resource.cancelBookingUseCase = cancelUseCase;
        resource.getBookingsUseCase = getUseCase;
        resource.bookingDtoMapper = new BookingDtoMapper();
    }

    @Test
    void shouldCreateBooking() {
        Booking booking = booking(1L, 10L, 100L, BookingStatus.CONFIRMED);
        createUseCase.response = booking;

        CreateBookingRequest request = new CreateBookingRequest();
        request.setUserId(100L);

        Response response = resource.createBooking(10L, "key-1", request);

        assertEquals(201, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Booking created", body.getMessage());
    }

    @Test
    void shouldCancelBooking() {
        Booking cancelled = booking(1L, 10L, 100L, BookingStatus.CANCELLED);
        Booking promoted = booking(2L, 10L, 101L, BookingStatus.CONFIRMED);
        cancelUseCase.response = new CancelBookingUseCase.CancelBookingResult(cancelled, promoted);

        Response response = resource.cancelBooking(1L, "key-2");

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Booking cancelled", body.getMessage());
    }

    @Test
    void shouldGetBookings() {
        getUseCase.response = new PageResponse<>(List.of(booking(1L, 10L, 100L, BookingStatus.CONFIRMED)), 0, 20, 1);

        Response response = resource.getBookings(null, null, null, null, null, null, null, 1, 20, "createdAt:desc");

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Bookings retrieved", body.getMessage());
    }

    private static class StubCreateBookingUseCase extends CreateBookingUseCase {
        Booking response;

        @Override
        public Booking execute(Long sessionId, Long userId, String requestId) {
            return response;
        }
    }

    private static class StubCancelBookingUseCase extends CancelBookingUseCase {
        CancelBookingResult response;

        @Override
        public CancelBookingResult execute(Long bookingId, String requestId) {
            return response;
        }
    }

    private static class StubGetBookingsUseCase extends GetBookingsUseCase {
        PageResponse<Booking> response;

        @Override
        public PageResponse<Booking> execute(Long sessionId, Long userId, BookingStatus status, Long branchId,
                                             Long activityId, Instant from, Instant to, int page, int limit,
                                             String sort) {
            return response;
        }
    }

    private static Booking booking(Long id, Long sessionId, Long userId, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setSessionId(sessionId);
        booking.setUserId(userId);
        booking.setStatus(status);
        return booking;
    }
}
