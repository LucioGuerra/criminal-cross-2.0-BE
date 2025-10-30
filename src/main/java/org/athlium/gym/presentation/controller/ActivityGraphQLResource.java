package org.athlium.gym.presentation.controller;

import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.athlium.gym.application.usecase.*;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.presentation.dto.*;
import org.athlium.gym.presentation.mapper.ActivityDtoMapper;

import java.util.List;

@GraphQLApi
public class ActivityGraphQLResource {

    @Inject
    CreateActivityUseCase createActivityUseCase;

    @Inject
    GetActivityUseCase getActivityUseCase;

    @Inject
    UpdateActivityUseCase updateActivityUseCase;

    @Inject
    DeleteActivityUseCase deleteActivityUseCase;

    @Inject
    GetActivitiesUseCase getActivitiesUseCase;

    @Inject
    ActivityDtoMapper dtoMapper;

    @Query("activity")
    @Description("Get a single activity by its ID")
    public ActivityResponse getActivity(@Name("id") @Description("Activity ID") Long id) {
        Activity activity = getActivityUseCase.execute(id);
        return dtoMapper.toResponse(activity);
    }

    @Query("activities")
    @Description("Get paginated activities by tenant")
    public ActivityPageResponse getActivities(
            @Name("tenantId") @Description("Tenant ID") Long tenantId,
            @Name("isActive") @Description("Filter by active status") Boolean isActive,
            @Name("page") @Description("Page number (0-based)") int page,
            @Name("size") @Description("Page size") int size) {
        var pageResponse = getActivitiesUseCase.executeByTenant(tenantId, isActive, page, size);
        return dtoMapper.toPageResponse(pageResponse);
    }

    @Query("activitiesByName")
    @Description("Search activities by name with pagination")
    public ActivityPageResponse getActivitiesByName(
            @Name("name") @Description("Activity name to search") String name,
            @Name("tenantId") @Description("Tenant ID") Long tenantId,
            @Name("page") @Description("Page number (0-based)") int page,
            @Name("size") @Description("Page size") int size) {
        var pageResponse = getActivitiesUseCase.executeByName(name, tenantId, page, size);
        return dtoMapper.toPageResponse(pageResponse);
    }

    @Query("allActivities")
    @Description("Get all activities by tenant without pagination")
    public List<ActivityResponse> getAllActivities(
            @Name("tenantId") @Description("Tenant ID") Long tenantId,
            @Name("isActive") @Description("Filter by active status") Boolean isActive) {
        List<Activity> activities = getActivitiesUseCase.executeAllByTenant(tenantId, isActive);
        return dtoMapper.toResponseList(activities);
    }

    @Mutation("createActivity")
    @Description("Create a new activity")
    public ActivityResponse createActivity(
            @Name("input") @Description("Activity data") ActivityInput input) {
        Activity created = createActivityUseCase.execute(input.getName(), input.getDescription(), input.getTenantId());
        return dtoMapper.toResponse(created);
    }

    @Mutation("updateActivity")
    @Description("Update an existing activity")
    public ActivityResponse updateActivity(
            @Name("input") @Description("Updated activity data") ActivityUpdateInput input) {
        Activity activity = dtoMapper.toDomain(input);
        Activity updated = updateActivityUseCase.execute(activity);
        return dtoMapper.toResponse(updated);
    }

    @Mutation("deleteActivity")
    @Description("Delete an activity by ID")
    public Boolean deleteActivity(
            @Name("id") @Description("Activity ID to delete") Long id) {
        deleteActivityUseCase.execute(id);
        return true;
    }
}