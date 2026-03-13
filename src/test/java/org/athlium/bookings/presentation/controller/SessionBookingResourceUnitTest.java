package org.athlium.bookings.presentation.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.security.SecurityContext;
import org.athlium.bookings.application.usecase.CreateBookingUseCase;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.presentation.dto.CreateBookingRequest;
import org.athlium.bookings.presentation.mapper.BookingDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.users.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionBookingResourceUnitTest {

    private SessionBookingResource resource;
    private StubCreateBookingUseCase createUseCase;
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        resource = new SessionBookingResource();
        createUseCase = new StubCreateBookingUseCase();
        securityContext = new SecurityContext();

        resource.createBookingUseCase = createUseCase;
        resource.bookingDtoMapper = new BookingDtoMapper();
        resource.securityContext = securityContext;
    }

    @Test
    void shouldCreateBooking() {
        authenticateAs(100L, Role.CLIENT);

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
        assertEquals(100L, createUseCase.lastUserId);
    }

    @Test
    void shouldDefaultToCurrentUserWhenRequestUserIdIsMissing() {
        authenticateAs(100L, Role.CLIENT);
        createUseCase.response = bookingResponse();

        Response response = resource.createBooking(10L, "key-2", new CreateBookingRequest());

        assertEquals(201, response.getStatus());
        assertEquals(100L, createUseCase.lastUserId);
    }

    @Test
    void shouldRejectClientBookingForAnotherUser() {
        authenticateAs(100L, Role.CLIENT);
        createUseCase.response = bookingResponse();

        CreateBookingRequest request = new CreateBookingRequest();
        request.setUserId(999L);

        Response response = resource.createBooking(10L, "key-3", request);

        assertEquals(403, response.getStatus());
    }

    @Test
    void shouldAllowProfessorToBookAnotherUser() {
        authenticateAs(200L, Role.PROFESSOR);
        createUseCase.response = bookingResponse();

        CreateBookingRequest request = new CreateBookingRequest();
        request.setUserId(999L);

        Response response = resource.createBooking(10L, "key-4", request);

        assertEquals(201, response.getStatus());
        assertEquals(999L, createUseCase.lastUserId);
    }

    private Booking bookingResponse() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setSessionId(10L);
        booking.setUserId(100L);
        booking.setStatus(BookingStatus.CONFIRMED);
        return booking;
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

    private static class StubCreateBookingUseCase extends CreateBookingUseCase {
        Booking response;
        Long lastUserId;

        @Override
        public Booking execute(Long sessionId, Long userId, String requestId) {
            this.lastUserId = userId;
            return response;
        }
    }
}
