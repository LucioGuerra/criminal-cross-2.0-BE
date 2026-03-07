package org.athlium.clients.domain.model;

import org.athlium.gym.domain.model.Activity;

public class ClientPackageCredit {

    private Long activityId;
    private Activity activity;
    private Integer tokens;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Integer getTokens() {
        return tokens;
    }

    public void setTokens(Integer tokens) {
        this.tokens = tokens;
    }
}
