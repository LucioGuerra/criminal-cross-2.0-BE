package org.athlium.gym.presentation.dto;

import lombok.Data;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Name;

import java.util.List;

@Data
@Name("ActivityPage")
@Description("Paginated list of activities")
public class ActivityPageResponse {
    @Description("List of activities in this page")
    private List<ActivityResponse> content;
    
    @Description("Current page number (0-based)")
    private int page;
    
    @Description("Number of items per page")
    private int size;
    
    @Description("Total number of activities")
    private long totalElements;
    
    @Description("Total number of pages")
    private int totalPages;
}