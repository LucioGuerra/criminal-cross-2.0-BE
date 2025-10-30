package org.athlium.gym.presentation.dto;

import lombok.Data;

@Data
public class ActivityResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private String tenantId;
}