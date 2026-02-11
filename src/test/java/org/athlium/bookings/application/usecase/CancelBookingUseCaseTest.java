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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CancelBookingUseCaseTest {

    private CancelBookingUseCase useCase;
    private InMemoryBookingRepository bookingRepository;
    private InMemorySessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        useCase = new CancelBookingUseCase();
        bookingRepository = new InMemoryBookingRepository();
        sessionRepository = new InMemorySessionRepository();
        useCase.bookingRepository = bookingRepository;
        useCase.sessionInstanceRepository = sessionRepository;
    }

    @Test
    void shouldPromoteFirstWaitlistedBookingAfterConfirmedCancellation() {
        Booking confirmed = booking(1L, 10L, 100L, BookingStatus.CONFIRMED, Instant.parse("2026-01-01T10:00:00Z"));
        Booking waitlist1 = booking(2L, 10L, 101L, BookingStatus.WAITLISTED, Instant.parse("2026-01-01T10:01:00Z"));
        Booking waitlist2 = booking(3L, 10L, 102L, BookingStatus.WAITLISTED, Instant.parse("2026-01-01T10:02:00Z"));
        bookingRepository.bookings.addAll(List.of(confirmed, waitlist1, waitlist2));

        var result = useCase.execute(1L, "cancel-1");

        assertEquals(BookingStatus.CANCELLED, result.cancelledBooking().getStatus());
        assertNotNull(result.promotedBooking());
        assertEquals(2L, result.promotedBooking().getId());
        assertEquals(BookingStatus.CONFIRMED, result.promotedBooking().getStatus());
    }

    @Test
    void shouldNotPromoteWhenCancellingWaitlistedBooking() {
        Booking waitlisted = booking(4L, 10L, 100L, BookingStatus.WAITLISTED, Instant.parse("2026-01-01T10:00:00Z"));
        bookingRepository.bookings.add(waitlisted);

        var result = useCase.execute(4L, "cancel-2");

        assertEquals(BookingStatus.CANCELLED, result.cancelledBooking().getStatus());
        assertNull(result.promotedBooking());
    }

    @Test
    void shouldReturnSamePromotedBookingOnCancelRetryWithSameKey() {
        Booking confirmed = booking(10L, 20L, 200L, BookingStatus.CONFIRMED, Instant.parse("2026-01-01T10:00:00Z"));
        Booking waitlist = booking(11L, 20L, 201L, BookingStatus.WAITLISTED, Instant.parse("2026-01-01T10:01:00Z"));
        bookingRepository.bookings.addAll(List.of(confirmed, waitlist));

        var first = useCase.execute(10L, "same-cancel-key");
        var second = useCase.execute(10L, "same-cancel-key");

        assertNotNull(first.promotedBooking());
        assertNotNull(second.promotedBooking());
        assertEquals(first.promotedBooking().getId(), second.promotedBooking().getId());
    }

    @Test
    void shouldRejectWhenCancelIdempotencyKeyIsReusedForDifferentBooking() {
        Booking first = booking(20L, 30L, 300L, BookingStatus.CONFIRMED, Instant.parse("2026-01-01T10:00:00Z"));
        Booking second = booking(21L, 30L, 301L, BookingStatus.CONFIRMED, Instant.parse("2026-01-01T10:01:00Z"));
        bookingRepository.bookings.addAll(List.of(first, second));

        useCase.execute(20L, "same-cancel-key");

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> useCase.execute(21L, "same-cancel-key")
        );

        assertEquals("Idempotency key already used for a different cancel request", ex.getMessage());
    }

    @Test
    void shouldRejectWhenBookingAlreadyCancelledWithoutIdempotencyKey() {
        Booking cancelled = booking(30L, 40L, 400L, BookingStatus.CANCELLED, Instant.parse("2026-01-01T10:00:00Z"));
        bookingRepository.bookings.add(cancelled);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(30L, null));

        assertEquals("Booking is already cancelled", ex.getMessage());
    }

    @Test
    void shouldRejectWhenCancelIdempotencyKeyIsTooLong() {
        Booking confirmed = booking(40L, 50L, 500L, BookingStatus.CONFIRMED, Instant.parse("2026-01-01T10:00:00Z"));
        bookingRepository.bookings.add(confirmed);

        String longKey = "x".repeat(129);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> useCase.execute(40L, longKey));

        assertEquals("Idempotency key length must be less than or equal to 128", ex.getMessage());
    }

    private static class InMemoryBookingRepository implements BookingRepository {
        private final List<Booking> bookings = new ArrayList<>();

        @Override
        public Booking save(Booking booking) {
            if (booking.getId() == null) {
                booking.setId((long) (bookings.size() + 1));
                bookings.add(booking);
                return booking;
            }

            bookings.removeIf(existing -> existing.getId().equals(booking.getId()));
            bookings.add(booking);
            return booking;
        }

        @Override
        public Optional<Booking> findById(Long id) {
            return bookings.stream().filter(b -> b.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Booking> findByIdForUpdate(Long id) {
            return findById(id);
        }

        @Override
        public Optional<Booking> findByCreateRequestId(String requestId) {
            return Optional.empty();
        }

        @Override
        public Optional<Booking> findByCancelRequestId(String requestId) {
            return bookings.stream().filter(b -> requestId.equals(b.getCancelRequestId())).findFirst();
        }

        @Override
        public boolean existsActiveBooking(Long sessionId, Long userId) {
            return false;
        }

        @Override
        public long countBySessionAndStatus(Long sessionId, BookingStatus status) {
            return 0;
        }

        @Override
        public Optional<Booking> findFirstWaitlistedBySessionId(Long sessionId) {
            return bookings.stream()
                    .filter(b -> b.getSessionId().equals(sessionId) && b.getStatus() == BookingStatus.WAITLISTED)
                    .min(Comparator.comparing(Booking::getCreatedAt));
        }

        @Override
        public Optional<Booking> findFirstWaitlistedBySessionIdForUpdate(Long sessionId) {
            return findFirstWaitlistedBySessionId(sessionId);
        }

        @Override
        public PageResponse<Booking> findBookings(Long sessionId, Long userId, BookingStatus status, Long branchId,
                                                  Long activityId, Instant from, Instant to, int page,
                                                  int size, boolean sortAscending) {
            return new PageResponse<>(List.of(), page, size, 0);
        }
    }

    private static class InMemorySessionRepository implements SessionInstanceRepository {

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
            SessionInstance session = new SessionInstance();
            session.setId(id);
            session.setStatus(SessionStatus.OPEN);
            return Optional.of(session);
        }

        @Override
        public Optional<SessionInstance> findByIdForUpdate(Long id) {
            return findById(id);
        }

        @Override
        public PageResponse<SessionInstance> findSessions(Long organizationId,
                                                          Long headquartersId,
                                                          Long activityId,
                                                          SessionStatus status,
                                                          Instant from,
                                                          Instant to,
                                                          int page,
                                                          int size,
                                                          boolean sortAscending) {
            return new PageResponse<>(List.of(), page, size, 0);
        }
    }

    private static Booking booking(Long id, Long sessionId, Long userId, BookingStatus status, Instant createdAt) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setSessionId(sessionId);
        booking.setUserId(userId);
        booking.setStatus(status);
        booking.setCreatedAt(createdAt);
        return booking;
    }
}
