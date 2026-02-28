package org.athlium.clients.presentation.dto;

public class ClientPackageCreditResponse {

    private Long activityId;
    private Integer tokens;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Integer getTokens() {
        return tokens;
    }

    public void setTokens(Integer tokens) {
        this.tokens = tokens;
    }
}
