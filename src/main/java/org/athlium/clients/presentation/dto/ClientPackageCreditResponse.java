package org.athlium.clients.presentation.dto;

import org.athlium.gym.presentation.dto.ActivityResponse;

public class ClientPackageCreditResponse {

    private ActivityResponse activity;
    private Integer tokens;

    public ActivityResponse getActivity() {
        return activity;
    }

    public void setActivity(ActivityResponse activity) {
        this.activity = activity;
    }

    public Integer getTokens() {
        return tokens;
    }

    public void setTokens(Integer tokens) {
        this.tokens = tokens;
    }
}
