package org.athlium.bookings.presentation.dto;

public class CancelBookingResponse {

    private BookingResponse cancelledBooking;
    private BookingResponse promotedBooking;

    public BookingResponse getCancelledBooking() {
        return cancelledBooking;
    }

    public void setCancelledBooking(BookingResponse cancelledBooking) {
        this.cancelledBooking = cancelledBooking;
    }

    public BookingResponse getPromotedBooking() {
        return promotedBooking;
    }

    public void setPromotedBooking(BookingResponse promotedBooking) {
        this.promotedBooking = promotedBooking;
    }
}
