package org.athlium.gym.presentation.dto;

import lombok.Data;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Name;

@Data
@Name("ActivityInput")
@Description("Input data for creating a new activity")
public class ActivityInput {
    @Description("Name of the activity")
    private String name;
    
    @Description("Detailed description of the activity")
    private String description;
    
    @Description("Tenant identifier")
    private String hqId;
}