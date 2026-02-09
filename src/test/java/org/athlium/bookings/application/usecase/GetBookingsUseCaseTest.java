package org.athlium.bookings.application.usecase;

import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetBookingsUseCaseTest {

    private GetBookingsUseCase useCase;
    private StubBookingRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new GetBookingsUseCase();
        repository = new StubBookingRepository();
        useCase.bookingRepository = repository;
    }

    @Test
    void shouldRejectInvalidSort() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                useCase.execute(null, null, null, null, null, null, null, 1, 20, "id:asc"));

        assertEquals("sort must be createdAt:asc or createdAt:desc", ex.getMessage());
    }

    @Test
    void shouldPassDescendingSortAndZeroBasedPage() {
        PageResponse<Booking> response = useCase.execute(
                1L,
                10L,
                BookingStatus.CONFIRMED,
                null,
                null,
                null,
                null,
                2,
                10,
                "createdAt:desc"
        );

        assertEquals(1, repository.capturedPage);
        assertFalse(repository.capturedSortAscending);
        assertEquals(1, response.getPage());
    }

    private static class StubBookingRepository implements BookingRepository {
        int capturedPage;
        boolean capturedSortAscending;

        @Override
        public Booking save(Booking booking) {
            return booking;
        }

        @Override
        public Optional<Booking> findById(Long id) {
            return Optional.empty();
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
            return Optional.empty();
        }

        @Override
        public PageResponse<Booking> findBookings(Long sessionId, Long userId, BookingStatus status, Long branchId,
                                                  Long activityId, Instant from, Instant to, int page,
                                                  int size, boolean sortAscending) {
            this.capturedPage = page;
            this.capturedSortAscending = sortAscending;
            return new PageResponse<>(List.of(), page, size, 0);
        }
    }
}
