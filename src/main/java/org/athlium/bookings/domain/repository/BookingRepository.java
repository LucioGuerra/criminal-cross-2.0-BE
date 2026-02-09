package org.athlium.bookings.domain.repository;

import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.shared.domain.PageResponse;

import java.time.Instant;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    Optional<Booking> findByIdForUpdate(Long id);

    boolean existsActiveBooking(Long sessionId, Long userId);

    long countBySessionAndStatus(Long sessionId, BookingStatus status);

    Optional<Booking> findFirstWaitlistedBySessionId(Long sessionId);

    Optional<Booking> findFirstWaitlistedBySessionIdForUpdate(Long sessionId);

    PageResponse<Booking> findBookings(
            Long sessionId,
            Long userId,
            BookingStatus status,
            Long branchId,
            Long activityId,
            Instant from,
            Instant to,
            int page,
            int size,
            boolean sortAscending
    );
}
