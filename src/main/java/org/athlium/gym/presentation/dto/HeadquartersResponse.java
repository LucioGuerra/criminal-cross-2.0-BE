package org.athlium.gym.presentation.dto;

import lombok.Data;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Ignore;
import org.eclipse.microprofile.graphql.Name;

import java.util.List;

@Data
@Name("Headquarters")
@Description("Headquarters information")
public class HeadquartersResponse {
    @Description("Unique identifier of the headquarters")
    private Long id;
    
    @Description("Organization ID that owns this headquarters")
    private Long organizationId;
    
    @Description("Name of the headquarters")
    private String name;
    
    @Ignore // Loaded dynamically by ActivityResolver
    private List<ActivityResponse> activities;
    
    @Ignore // Loaded dynamically by HeadquartersResolver
    private OrganizationResponse organization;
}