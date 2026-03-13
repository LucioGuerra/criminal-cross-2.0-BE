package org.athlium.gym.presentation.dto;

import lombok.Data;

import java.util.List;

@Data
public class ActivityResponse {
    private Long id;

    private String name;

    private String description;

    private Boolean isActive;

    private Long hqId;

    private List<SessionResponse> sessions;
}
