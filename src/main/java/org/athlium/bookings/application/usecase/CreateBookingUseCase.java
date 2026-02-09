package org.athlium.bookings.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class CreateBookingUseCase {

    @Inject
    BookingRepository bookingRepository;

    @Inject
    SessionInstanceRepository sessionInstanceRepository;

    @Transactional
    public Booking execute(Long sessionId, Long userId) {
        validateIds(sessionId, userId);

        var session = sessionInstanceRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session", sessionId));

        if (session.getStatus() != SessionStatus.OPEN) {
            throw new BadRequestException("Session is not open for bookings");
        }

        if (bookingRepository.existsActiveBooking(sessionId, userId)) {
            throw new BadRequestException("User already has an active booking for this session");
        }

        BookingStatus targetStatus = resolveStatusForNewBooking(sessionId, session.getMaxParticipants(),
                session.getWaitlistEnabled(), session.getWaitlistMaxSize());

        Booking booking = new Booking();
        booking.setSessionId(sessionId);
        booking.setUserId(userId);
        booking.setStatus(targetStatus);
        return bookingRepository.save(booking);
    }

    private void validateIds(Long sessionId, Long userId) {
        if (sessionId == null || sessionId <= 0) {
            throw new BadRequestException("sessionId must be a positive number");
        }
        if (userId == null || userId <= 0) {
            throw new BadRequestException("userId must be a positive number");
        }
    }

    private BookingStatus resolveStatusForNewBooking(
            Long sessionId,
            Integer maxParticipants,
            Boolean waitlistEnabled,
            Integer waitlistMaxSize
    ) {
        long confirmedCount = bookingRepository.countBySessionAndStatus(sessionId, BookingStatus.CONFIRMED);
        int max = maxParticipants == null ? 0 : maxParticipants;

        if (confirmedCount < max) {
            return BookingStatus.CONFIRMED;
        }

        if (Boolean.TRUE.equals(waitlistEnabled)) {
            long waitlistedCount = bookingRepository.countBySessionAndStatus(sessionId, BookingStatus.WAITLISTED);
            int maxWaitlist = waitlistMaxSize == null ? 0 : waitlistMaxSize;
            if (waitlistedCount < maxWaitlist) {
                return BookingStatus.WAITLISTED;
            }
            throw new BadRequestException("Waitlist is full for this session");
        }

        throw new BadRequestException("Session is full");
    }
}
