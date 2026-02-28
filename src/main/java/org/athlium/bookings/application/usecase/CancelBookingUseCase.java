package org.athlium.bookings.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.clients.application.service.ClientPackageCreditService;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

import java.time.Instant;

@ApplicationScoped
public class CancelBookingUseCase {

    @Inject
    BookingRepository bookingRepository;

    @Inject
    SessionInstanceRepository sessionInstanceRepository;

    @Inject
    ClientPackageCreditService clientPackageCreditService;

    @Transactional
    public CancelBookingResult execute(Long bookingId, String requestId) {
        if (bookingId == null || bookingId <= 0) {
            throw new BadRequestException("bookingId must be a positive number");
        }
        String normalizedRequestId = normalizeRequestId(requestId);

        if (normalizedRequestId != null) {
            var existingByRequest = bookingRepository.findByCancelRequestId(normalizedRequestId);
            if (existingByRequest.isPresent()) {
                Booking existing = existingByRequest.get();
                if (!existing.getId().equals(bookingId)) {
                    throw new BadRequestException("Idempotency key already used for a different cancel request");
                }
                Booking promotedOnOriginal = existing.getPromotedBookingId() != null
                        ? bookingRepository.findById(existing.getPromotedBookingId()).orElse(null)
                        : null;
                return new CancelBookingResult(existing, promotedOnOriginal);
            }
        }

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));

        var session = sessionInstanceRepository.findByIdForUpdate(booking.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session", booking.getSessionId()));

        if (session.getActivityId() == null || session.getActivityId() <= 0) {
            throw new BadRequestException("Session activityId must be defined to cancel bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.WAITLISTED) {
            throw new BadRequestException("Booking cannot be cancelled from current status");
        }

        BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancelRequestId(normalizedRequestId);
        Booking cancelled = bookingRepository.save(booking);

        if (previousStatus == BookingStatus.CONFIRMED) {
            clientPackageCreditService.refundCredit(
                    cancelled.getUserId(),
                    session.getActivityId(),
                    cancelled.getConsumedPackageId()
            );
        }

        Booking promoted = null;
        if (previousStatus == BookingStatus.CONFIRMED) {
            var waitlisted = bookingRepository.findFirstWaitlistedBySessionIdForUpdate(booking.getSessionId());
            if (waitlisted.isPresent()) {
                Booking candidate = waitlisted.get();
                if (!clientPackageCreditService.hasAvailableCredit(candidate.getUserId(), session.getActivityId())) {
                    // TODO: Emit domain event to notify/handle waitlist user without credits.
                    throw new BadRequestException("Waitlisted user has no available credits for this activity");
                }

                Long consumedPackageId = clientPackageCreditService.consumeCredit(candidate.getUserId(), session.getActivityId());
                candidate.setStatus(BookingStatus.CONFIRMED);
                candidate.setConsumedPackageId(consumedPackageId);
                promoted = bookingRepository.save(candidate);
                cancelled.setPromotedBookingId(promoted.getId());
                cancelled = bookingRepository.save(cancelled);
            }
        }

        return new CancelBookingResult(cancelled, promoted);
    }

    private String normalizeRequestId(String requestId) {
        if (requestId == null) {
            return null;
        }
        String normalized = requestId.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 128) {
            throw new BadRequestException("Idempotency key length must be less than or equal to 128");
        }
        return normalized;
    }

    public record CancelBookingResult(Booking cancelledBooking, Booking promotedBooking) {
    }
}
