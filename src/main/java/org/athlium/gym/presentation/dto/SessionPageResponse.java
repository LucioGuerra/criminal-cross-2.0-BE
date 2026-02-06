package org.athlium.gym.presentation.dto;

import lombok.Data;

import java.util.List;

@Data
public class SessionPageResponse {
    private List<SessionResponse> items;
    private int page;
    private int limit;
    private long total;

    public SessionPageResponse(List<SessionResponse> items, int page, int limit, long total) {
        this.items = items;
        this.page = page;
        this.limit = limit;
        this.total = total;
    }
}
