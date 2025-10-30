package org.athlium.gym.presentation.dto;

import lombok.Data;

@Data
public class ActivityInput {
    private String name;
    private String description;
    private Boolean isActive;
    private String tenantId;
}