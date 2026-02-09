package org.athlium.gym.presentation.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrganizationResponse {
    private Long id;

    private String name;

    private List<HeadquartersResponse> headquarters;
}
