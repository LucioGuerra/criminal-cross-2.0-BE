package org.athlium.bookings.presentation.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.security.SecurityContext;
import org.athlium.bookings.application.usecase.CancelBookingUseCase;
import org.athlium.bookings.application.usecase.GetBookingsUseCase;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.bookings.presentation.mapper.BookingDtoMapper;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.users.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingResourceUnitTest {

    private BookingResource resource;
    private StubCancelBookingUseCase cancelUseCase;
    private StubGetBookingsUseCase getUseCase;
    private SecurityContext securityContext;
    private StubBookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        resource = new BookingResource();
        cancelUseCase = new StubCancelBookingUseCase();
        getUseCase = new StubGetBookingsUseCase();
        securityContext = new SecurityContext();
        bookingRepository = new StubBookingRepository();

        resource.cancelBookingUseCase = cancelUseCase;
        resource.getBookingsUseCase = getUseCase;
        resource.bookingDtoMapper = new BookingDtoMapper();
        resource.securityContext = securityContext;
        resource.bookingRepository = bookingRepository;
    }

    @Test
    void shouldCancelBooking() {
        authenticateAs(100L, Role.CLIENT);

        Booking cancelled = booking(1L, 10L, 100L, BookingStatus.CANCELLED);
        Booking promoted = booking(2L, 10L, 101L, BookingStatus.CONFIRMED);
        bookingRepository.bookingToReturn = booking(1L, 10L, 100L, BookingStatus.CONFIRMED);
        cancelUseCase.response = new CancelBookingUseCase.CancelBookingResult(cancelled, promoted);

        Response response = resource.cancelBooking(1L, "key-2");

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Booking cancelled", body.getMessage());
    }

    @Test
    void shouldGetBookings() {
        authenticateAs(100L, Role.CLIENT);
        getUseCase.response = new PageResponse<>(List.of(booking(1L, 10L, 100L, BookingStatus.CONFIRMED)), 0, 20, 1);

        Response response = resource.getBookings(null, null, null, null, null, null, null, 1, 20, "createdAt:desc");

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Bookings retrieved", body.getMessage());
        assertEquals(100L, getUseCase.lastUserId);
    }

    @Test
    void shouldRejectClientGettingOtherUserBookings() {
        authenticateAs(100L, Role.CLIENT);
        getUseCase.response = new PageResponse<>(List.of(), 0, 20, 0);

        Response response = resource.getBookings(null, 999L, null, null, null, null, null, 1, 20, "createdAt:desc");

        assertEquals(403, response.getStatus());
    }

    @Test
    void shouldAllowProfessorGettingOtherUserBookings() {
        authenticateAs(200L, Role.PROFESSOR);
        getUseCase.response = new PageResponse<>(List.of(), 0, 20, 0);

        Response response = resource.getBookings(null, 999L, null, null, null, null, null, 1, 20, "createdAt:desc");

        assertEquals(200, response.getStatus());
        assertEquals(999L, getUseCase.lastUserId);
    }

    @Test
    void shouldRejectClientCancellingOtherUserBooking() {
        authenticateAs(100L, Role.CLIENT);
        bookingRepository.bookingToReturn = booking(1L, 10L, 999L, BookingStatus.CONFIRMED);

        Response response = resource.cancelBooking(1L, "key-3");

        assertEquals(403, response.getStatus());
    }

    @Test
    void shouldAllowOrgAdminCancellingOtherUserBooking() {
        authenticateAs(500L, Role.ORG_ADMIN);
        bookingRepository.bookingToReturn = booking(1L, 10L, 999L, BookingStatus.CONFIRMED);
        cancelUseCase.response = new CancelBookingUseCase.CancelBookingResult(
                booking(1L, 10L, 999L, BookingStatus.CANCELLED),
                null
        );

        Response response = resource.cancelBooking(1L, "key-4");

        assertEquals(200, response.getStatus());
    }

    private void authenticateAs(Long userId, Role... roles) {
        securityContext.setCurrentUser(AuthenticatedUser.builder()
                .firebaseUid("uid-" + userId)
                .email("user" + userId + "@test.com")
                .name("Test")
                .emailVerified(true)
                .provider(AuthProvider.EMAIL)
                .userId(userId)
                .roles(Set.of(roles))
                .active(true)
                .build());
        securityContext.setAuthenticated(true);
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
        Long lastUserId;

        @Override
        public PageResponse<Booking> execute(Long sessionId, Long userId, BookingStatus status, Long branchId,
                                             Long activityId, Instant from, Instant to, int page, int limit,
                                             String sort) {
            this.lastUserId = userId;
            return response;
        }
    }

    private static class StubBookingRepository implements BookingRepository {
        Booking bookingToReturn;

        @Override
        public Booking save(Booking booking) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Booking> findById(Long id) {
            return Optional.ofNullable(bookingToReturn);
        }

        @Override
        public Optional<Booking> findByIdForUpdate(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Booking> findByCreateRequestId(String requestId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Booking> findByCancelRequestId(String requestId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsActiveBooking(Long sessionId, Long userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countBySessionAndStatus(Long sessionId, BookingStatus status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Booking> findFirstWaitlistedBySessionId(Long sessionId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Booking> findFirstWaitlistedBySessionIdForUpdate(Long sessionId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PageResponse<Booking> findBookings(Long sessionId, Long userId, BookingStatus status, Long branchId,
                                                  Long activityId, Instant from, Instant to, int page, int size,
                                                  boolean sortAscending) {
            throw new UnsupportedOperationException();
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
