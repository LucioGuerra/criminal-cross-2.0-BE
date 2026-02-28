package org.athlium.clients.presentation.dto;

import java.util.Map;

public class ClientPackageUpsertRequest {

    private Long paymentId;
    private Map<String, Integer> activityTokens;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Map<String, Integer> getActivityTokens() {
        return activityTokens;
    }

    public void setActivityTokens(Map<String, Integer> activityTokens) {
        this.activityTokens = activityTokens;
    }
}
