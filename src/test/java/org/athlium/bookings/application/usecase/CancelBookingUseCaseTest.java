package org.athlium.bookings.application.usecase;

import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.shared.domain.PageResponse;
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

class CancelBookingUseCaseTest {

    private CancelBookingUseCase useCase;
    private InMemoryBookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        useCase = new CancelBookingUseCase();
        bookingRepository = new InMemoryBookingRepository();
        useCase.bookingRepository = bookingRepository;
    }

    @Test
    void shouldPromoteFirstWaitlistedBookingAfterConfirmedCancellation() {
        Booking confirmed = booking(1L, 10L, 100L, BookingStatus.CONFIRMED, Instant.parse("2026-01-01T10:00:00Z"));
        Booking waitlist1 = booking(2L, 10L, 101L, BookingStatus.WAITLISTED, Instant.parse("2026-01-01T10:01:00Z"));
        Booking waitlist2 = booking(3L, 10L, 102L, BookingStatus.WAITLISTED, Instant.parse("2026-01-01T10:02:00Z"));
        bookingRepository.bookings.addAll(List.of(confirmed, waitlist1, waitlist2));

        var result = useCase.execute(1L);

        assertEquals(BookingStatus.CANCELLED, result.cancelledBooking().getStatus());
        assertNotNull(result.promotedBooking());
        assertEquals(2L, result.promotedBooking().getId());
        assertEquals(BookingStatus.CONFIRMED, result.promotedBooking().getStatus());
    }

    @Test
    void shouldNotPromoteWhenCancellingWaitlistedBooking() {
        Booking waitlisted = booking(4L, 10L, 100L, BookingStatus.WAITLISTED, Instant.parse("2026-01-01T10:00:00Z"));
        bookingRepository.bookings.add(waitlisted);

        var result = useCase.execute(4L);

        assertEquals(BookingStatus.CANCELLED, result.cancelledBooking().getStatus());
        assertNull(result.promotedBooking());
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
        public PageResponse<Booking> findBookings(Long sessionId, Long userId, BookingStatus status, Long branchId,
                                                  Long activityId, Instant from, Instant to, int page,
                                                  int size, boolean sortAscending) {
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
