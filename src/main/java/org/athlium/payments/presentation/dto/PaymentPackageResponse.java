package org.athlium.payments.presentation.dto;

import java.util.List;

public class PaymentPackageResponse {

    private Long id;
    private List<PaymentPackageActivityResponse> activities;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<PaymentPackageActivityResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<PaymentPackageActivityResponse> activities) {
        this.activities = activities;
    }
}
