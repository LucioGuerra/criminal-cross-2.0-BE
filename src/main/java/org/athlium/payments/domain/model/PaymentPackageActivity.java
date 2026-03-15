package org.athlium.payments.domain.model;

import org.athlium.gym.domain.model.Activity;

public class PaymentPackageActivity {

    private final Activity activity;
    private final Integer weeklyFrequency;

    public PaymentPackageActivity(Activity activity, Integer weeklyFrequency) {
        this.activity = activity;
        this.weeklyFrequency = weeklyFrequency;
    }

    public Activity getActivity() {
        return activity;
    }

    public Integer getWeeklyFrequency() {
        return weeklyFrequency;
    }
}
