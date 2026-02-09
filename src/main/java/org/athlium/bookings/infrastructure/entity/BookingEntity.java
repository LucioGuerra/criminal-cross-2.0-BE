package org.athlium.bookings.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.athlium.bookings.domain.model.BookingStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "bookings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_bookings_create_request_id", columnNames = "create_request_id"),
                @UniqueConstraint(name = "uq_bookings_cancel_request_id", columnNames = "cancel_request_id")
        },
        indexes = {
                @Index(name = "idx_bookings_session_status", columnList = "session_id, status"),
                @Index(name = "idx_bookings_user_status", columnList = "user_id, status"),
                @Index(name = "idx_bookings_created_at", columnList = "created_at")
        }
)
public class BookingEntity extends PanacheEntity {

    @Column(name = "session_id", nullable = false)
    public Long sessionId;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BookingStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "cancelled_at")
    public Instant cancelledAt;

    @Column(name = "create_request_id", length = 128)
    public String createRequestId;

    @Column(name = "cancel_request_id", length = 128)
    public String cancelRequestId;
}
