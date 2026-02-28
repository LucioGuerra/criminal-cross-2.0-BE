package org.athlium.clients.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientPackage {

    private Long id;
    private Long userId;
    private Long paymentId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ClientPackageCredit> credits = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public List<ClientPackageCredit> getCredits() {
        return credits;
    }

    public void setCredits(List<ClientPackageCredit> credits) {
        this.credits = credits;
    }

    public boolean isExpired(LocalDate today) {
        return periodEnd != null && today.isAfter(periodEnd);
    }

    public void deactivate() {
        this.active = false;
    }
}
