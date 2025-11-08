package org.athlium.gym.presentation.dto;

import lombok.Data;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Name;

import java.util.List;

@Data
@Name("Organization")
@Description("Organization information")
public class OrganizationResponse {
    @Description("Unique identifier of the organization")
    private Long id;
    
    @Description("Name of the organization")
    private String name;
    
    @Description("List of headquarters belonging to this organization")
    private List<HeadquartersResponse> headquarters;
}