package org.athlium.clients.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "client_package_credits",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_client_package_activity", columnNames = {"package_id", "activity_id"})
        },
        indexes = {
                @Index(name = "idx_client_package_credits_package", columnList = "package_id"),
                @Index(name = "idx_client_package_credits_activity", columnList = "activity_id")
        }
)
public class ClientPackageCreditEntity extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    public ClientPackageEntity clientPackage;

    @Column(name = "activity_id", nullable = false)
    public Long activityId;

    @Column(name = "tokens", nullable = false)
    public Integer tokens;
}
