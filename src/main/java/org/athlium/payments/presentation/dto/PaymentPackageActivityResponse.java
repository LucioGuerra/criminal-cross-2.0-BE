package org.athlium.payments.presentation.dto;

import org.athlium.gym.presentation.dto.ActivityResponse;

public class PaymentPackageActivityResponse {

    private ActivityResponse activity;
    private Integer weeklyFrequency;

    public ActivityResponse getActivity() {
        return activity;
    }

    public void setActivity(ActivityResponse activity) {
        this.activity = activity;
    }

    public Integer getWeeklyFrequency() {
        return weeklyFrequency;
    }

    public void setWeeklyFrequency(Integer weeklyFrequency) {
        this.weeklyFrequency = weeklyFrequency;
    }
}
