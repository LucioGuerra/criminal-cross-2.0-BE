package org.athlium.bookings.application.usecase;

import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateBookingUseCaseTest {

    private CreateBookingUseCase useCase;
    private InMemoryBookingRepository bookingRepository;
    private InMemorySessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        useCase = new CreateBookingUseCase();
        bookingRepository = new InMemoryBookingRepository();
        sessionRepository = new InMemorySessionRepository();
        useCase.bookingRepository = bookingRepository;
        useCase.sessionInstanceRepository = sessionRepository;
    }

    @Test
    void shouldCreateConfirmedBookingWhenSlotAvailable() {
        sessionRepository.session.setStatus(SessionStatus.OPEN);
        sessionRepository.session.setMaxParticipants(2);
        bookingRepository.bookings.add(existingBooking(10L, 50L, BookingStatus.CONFIRMED));

        Booking created = useCase.execute(10L, 100L, "req-1");

        assertEquals(BookingStatus.CONFIRMED, created.getStatus());
    }

    @Test
    void shouldCreateWaitlistedBookingWhenSessionFull() {
        sessionRepository.session.setStatus(SessionStatus.OPEN);
        sessionRepository.session.setMaxParticipants(1);
        sessionRepository.session.setWaitlistEnabled(true);
        sessionRepository.session.setWaitlistMaxSize(3);
        bookingRepository.bookings.add(existingBooking(10L, 50L, BookingStatus.CONFIRMED));

        Booking created = useCase.execute(10L, 101L, "req-2");

        assertEquals(BookingStatus.WAITLISTED, created.getStatus());
    }

    @Test
    void shouldRejectWhenSessionIsFullAndWaitlistDisabled() {
        sessionRepository.session.setStatus(SessionStatus.OPEN);
        sessionRepository.session.setMaxParticipants(1);
        sessionRepository.session.setWaitlistEnabled(false);
        bookingRepository.bookings.add(existingBooking(10L, 50L, BookingStatus.CONFIRMED));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(10L, 102L, "req-3"));
        assertEquals("Session is full", ex.getMessage());
    }

    @Test
    void shouldReturnExistingBookingForSameIdempotencyKey() {
        sessionRepository.session.setStatus(SessionStatus.OPEN);
        sessionRepository.session.setMaxParticipants(5);

        Booking first = useCase.execute(10L, 100L, "same-key");
        Booking second = useCase.execute(10L, 100L, "same-key");

        assertEquals(first.getId(), second.getId());
    }

    private static class InMemoryBookingRepository implements BookingRepository {
        private final List<Booking> bookings = new ArrayList<>();

        @Override
        public Booking save(Booking booking) {
            booking.setId((long) (bookings.size() + 1));
            bookings.add(booking);
            return booking;
        }

        @Override
        public Optional<Booking> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public Optional<Booking> findByIdForUpdate(Long id) {
            return Optional.empty();
        }

        @Override
        public Optional<Booking> findByCreateRequestId(String requestId) {
            return bookings.stream().filter(b -> requestId.equals(b.getCreateRequestId())).findFirst();
        }

        @Override
        public Optional<Booking> findByCancelRequestId(String requestId) {
            return Optional.empty();
        }

        @Override
        public boolean existsActiveBooking(Long sessionId, Long userId) {
            return bookings.stream().anyMatch(b -> b.getSessionId().equals(sessionId)
                    && b.getUserId().equals(userId)
                    && (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.WAITLISTED));
        }

        @Override
        public long countBySessionAndStatus(Long sessionId, BookingStatus status) {
            return bookings.stream().filter(b -> b.getSessionId().equals(sessionId) && b.getStatus() == status).count();
        }

        @Override
        public Optional<Booking> findFirstWaitlistedBySessionId(Long sessionId) {
            return Optional.empty();
        }

        @Override
        public Optional<Booking> findFirstWaitlistedBySessionIdForUpdate(Long sessionId) {
            return Optional.empty();
        }

        @Override
        public PageResponse<Booking> findBookings(Long sessionId, Long userId, BookingStatus status, Long branchId,
                                                  Long activityId, Instant from, Instant to, int page,
                                                  int size, boolean sortAscending) {
            return new PageResponse<>(List.of(), page, size, 0);
        }
    }

    private static class InMemorySessionRepository implements SessionInstanceRepository {
        private final SessionInstance session = new SessionInstance();

        @Override
        public SessionInstance save(SessionInstance sessionInstance) {
            return sessionInstance;
        }

        @Override
        public boolean existsByOrganizationAndHeadquartersAndActivityAndStartsAt(Long organizationId,
                                                                                  Long headquartersId,
                                                                                  Long activityId,
                                                                                  Instant startsAt) {
            return false;
        }

        @Override
        public Optional<SessionInstance> findById(Long id) {
            session.setId(id);
            return Optional.of(session);
        }

        @Override
        public Optional<SessionInstance> findByIdForUpdate(Long id) {
            return findById(id);
        }

        @Override
        public PageResponse<SessionInstance> findSessions(Long organizationId, Long headquartersId,
                                                          Long activityId, SessionStatus status,
                                                          Instant from, Instant to, int page, int size,
                                                          boolean sortAscending) {
            return new PageResponse<>(List.of(), page, size, 0);
        }
    }

    private static Booking existingBooking(Long sessionId, Long userId, BookingStatus status) {
        Booking booking = new Booking();
        booking.setSessionId(sessionId);
        booking.setUserId(userId);
        booking.setStatus(status);
        return booking;
    }
}
