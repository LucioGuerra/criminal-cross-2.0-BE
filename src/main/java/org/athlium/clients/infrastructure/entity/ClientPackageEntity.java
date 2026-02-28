package org.athlium.clients.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "client_packages",
        indexes = {
                @Index(name = "idx_client_packages_user_active", columnList = "user_id, active"),
                @Index(name = "idx_client_packages_period_end", columnList = "period_end")
        }
)
public class ClientPackageEntity extends PanacheEntity {

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "payment_id")
    public Long paymentId;

    @Column(name = "period_start", nullable = false)
    public LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    public LocalDate periodEnd;

    @Column(name = "active", nullable = false)
    public Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;

    @OneToMany(mappedBy = "clientPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<ClientPackageCreditEntity> credits = new ArrayList<>();
}
