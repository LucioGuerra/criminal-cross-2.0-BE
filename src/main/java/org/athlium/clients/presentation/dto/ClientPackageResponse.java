package org.athlium.clients.presentation.dto;

import java.util.List;

public class ClientPackageResponse {

    private Long id;
    private Long userId;
    private Long paymentId;
    private String periodStart;
    private String periodEnd;
    private Boolean active;
    private List<ClientPackageCreditResponse> credits;

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

    public String getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(String periodStart) {
        this.periodStart = periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<ClientPackageCreditResponse> getCredits() {
        return credits;
    }

    public void setCredits(List<ClientPackageCreditResponse> credits) {
        this.credits = credits;
    }
}
