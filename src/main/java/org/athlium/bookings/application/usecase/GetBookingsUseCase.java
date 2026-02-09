package org.athlium.bookings.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;

import java.time.Instant;

@ApplicationScoped
public class GetBookingsUseCase {

    @Inject
    BookingRepository bookingRepository;

    public PageResponse<Booking> execute(
            Long sessionId,
            Long userId,
            BookingStatus status,
            Long branchId,
            Long activityId,
            Instant from,
            Instant to,
            int page,
            int limit,
            String sort
    ) {
        if (page < 1) {
            throw new BadRequestException("Page must be greater than or equal to 1");
        }
        if (limit < 1 || limit > 100) {
            throw new BadRequestException("Limit must be between 1 and 100");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("from must be less than or equal to to");
        }

        boolean sortAscending = parseSort(sort);

        return bookingRepository.findBookings(
                sessionId,
                userId,
                status,
                branchId,
                activityId,
                from,
                to,
                page - 1,
                limit,
                sortAscending
        );
    }

    private boolean parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return false;
        }

        String normalized = sort.trim().toLowerCase();
        if ("createdat:asc".equals(normalized)) {
            return true;
        }
        if ("createdat:desc".equals(normalized)) {
            return false;
        }

        throw new BadRequestException("sort must be createdAt:asc or createdAt:desc");
    }
}
