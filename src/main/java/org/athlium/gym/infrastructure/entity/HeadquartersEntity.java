package org.athlium.gym.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "headquarters")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HeadquartersEntity extends PanacheEntity {

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    private OrganizationEntity organization;

    @OneToMany(mappedBy = "headquarters", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ActivityEntity> activities;
}