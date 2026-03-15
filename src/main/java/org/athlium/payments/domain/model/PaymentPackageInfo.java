package org.athlium.payments.domain.model;

import java.util.List;

public class PaymentPackageInfo {

    private final Long id;
    private final List<PaymentPackageActivity> activities;

    public PaymentPackageInfo(Long id, List<PaymentPackageActivity> activities) {
        this.id = id;
        this.activities = activities;
    }

    public Long getId() {
        return id;
    }

    public List<PaymentPackageActivity> getActivities() {
        return activities;
    }
}
