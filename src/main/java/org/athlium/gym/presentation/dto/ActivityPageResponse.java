package org.athlium.gym.presentation.dto;

import lombok.Data;

import java.util.List;

@Data
public class ActivityPageResponse {
    private List<ActivityResponse> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;
}
