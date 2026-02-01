package org.athlium.gym.presentation.dto;

import lombok.Data;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Name;

@Data
@Name("Activity")
@Description("Activity information")
public class ActivityResponse {
    @Description("Unique identifier of the activity")
    private Long id;
    
    @Description("Name of the activity")
    private String name;
    
    @Description("Detailed description of the activity")
    private String description;
    
    @Description("Whether the activity is active or not")
    private Boolean isActive;
    
    @Description("Headquarters ID")
    private Long hqId;
}