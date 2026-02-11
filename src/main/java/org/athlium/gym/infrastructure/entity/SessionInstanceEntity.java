package org.athlium.gym.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.athlium.gym.domain.model.SessionSource;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.model.WaitlistStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "session_instances",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_session_instances_slot",
                        columnNames = {"organization_id", "headquarters_id", "activity_id", "starts_at"}
                )
        },
        indexes = {
                @Index(name = "idx_session_instances_hq_starts_at", columnList = "headquarters_id, starts_at"),
                @Index(name = "idx_session_instances_activity_starts_at", columnList = "activity_id, starts_at"),
                @Index(name = "idx_session_instances_status_starts_at", columnList = "status, starts_at")
        }
)
public class SessionInstanceEntity extends PanacheEntity {

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "headquarters_id", nullable = false)
    private Long headquartersId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionSource source;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "waitlist_enabled")
    private Boolean waitlistEnabled;

    @Column(name = "waitlist_max_size")
    private Integer waitlistMaxSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "waitlist_strategy")
    private WaitlistStrategy waitlistStrategy;

    @Column(name = "cancellation_min_hours_before_start")
    private Integer cancellationMinHoursBeforeStart;

    @Column(name = "cancellation_allow_late_cancel")
    private Boolean cancellationAllowLateCancel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
