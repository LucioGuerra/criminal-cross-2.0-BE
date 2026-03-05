package org.athlium.payments.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.athlium.payments.domain.model.PaymentMethod;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_paid_at", columnList = "paid_at"),
                @Index(name = "idx_payments_method", columnList = "method"),
                @Index(name = "idx_payments_client_id", columnList = "client_id"),
                @Index(name = "idx_payments_headquarters_id", columnList = "headquarters_id"),
                @Index(name = "idx_payments_organization_id", columnList = "organization_id")
        }
)
public class PaymentEntity extends PanacheEntity {

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    public BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 24)
    public PaymentMethod method;

    @Column(name = "paid_at", nullable = false)
    public LocalDate paidAt;

    @Column(name = "client_id", nullable = false)
    public Long clientId;

    @Column(name = "headquarters_id", nullable = false)
    public Long headquartersId;

    @Column(name = "organization_id", nullable = false)
    public Long organizationId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;
}
