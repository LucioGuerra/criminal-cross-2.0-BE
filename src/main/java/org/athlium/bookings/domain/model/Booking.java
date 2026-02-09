package org.athlium.bookings.domain.model;

import java.time.Instant;

public class Booking {

    private Long id;
    private Long sessionId;
    private Long userId;
    private BookingStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant cancelledAt;
    private String createRequestId;
    private String cancelRequestId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCreateRequestId() {
        return createRequestId;
    }

    public void setCreateRequestId(String createRequestId) {
        this.createRequestId = createRequestId;
    }

    public String getCancelRequestId() {
        return cancelRequestId;
    }

    public void setCancelRequestId(String cancelRequestId) {
        this.cancelRequestId = cancelRequestId;
    }
}
