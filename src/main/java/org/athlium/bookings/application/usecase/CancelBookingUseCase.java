package org.athlium.bookings.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

import java.time.Instant;

@ApplicationScoped
public class CancelBookingUseCase {

    @Inject
    BookingRepository bookingRepository;

    @Transactional
    public CancelBookingResult execute(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            throw new BadRequestException("bookingId must be a positive number");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.WAITLISTED) {
            throw new BadRequestException("Booking cannot be cancelled from current status");
        }

        BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        Booking cancelled = bookingRepository.save(booking);

        Booking promoted = null;
        if (previousStatus == BookingStatus.CONFIRMED) {
            var waitlisted = bookingRepository.findFirstWaitlistedBySessionId(booking.getSessionId());
            if (waitlisted.isPresent()) {
                Booking candidate = waitlisted.get();
                candidate.setStatus(BookingStatus.CONFIRMED);
                promoted = bookingRepository.save(candidate);
            }
        }

        return new CancelBookingResult(cancelled, promoted);
    }

    public record CancelBookingResult(Booking cancelledBooking, Booking promotedBooking) {
    }
}
