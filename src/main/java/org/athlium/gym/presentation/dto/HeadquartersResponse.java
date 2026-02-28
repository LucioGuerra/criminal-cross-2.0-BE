package org.athlium.gym.presentation.dto;

import lombok.Data;

import java.util.List;

@Data
public class HeadquartersResponse {
    private Long id;

    private Long organizationId;

    private String name;

    private List<ActivityResponse> activities;

    private OrganizationResponse organization;
}
