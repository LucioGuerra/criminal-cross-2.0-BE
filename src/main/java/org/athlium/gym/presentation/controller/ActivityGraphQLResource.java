package org.athlium.gym.presentation.controller;

import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
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
    public ActivityResponse getActivity(Long id) {
        Activity activity = getActivityUseCase.execute(id);
        return dtoMapper.toResponse(activity);
    }

    @Query("activities")
    public ActivityPageResponse getActivities(Long tenantId, Boolean isActive, int page, int size) {
        var pageResponse = getActivitiesUseCase.executeByTenant(tenantId, isActive, page, size);
        return dtoMapper.toPageResponse(pageResponse);
    }

    @Query("activitiesByName")
    public ActivityPageResponse getActivitiesByName(String name, int page, int size) {
        var pageResponse = getActivitiesUseCase.executeByName(name, page, size);
        return dtoMapper.toPageResponse(pageResponse);
    }

    @Query("allActivities")
    public List<ActivityResponse> getAllActivities(Long tenantId, Boolean isActive) {
        List<Activity> activities = getActivitiesUseCase.executeAllByTenant(tenantId, isActive);
        return dtoMapper.toResponseList(activities);
    }

    @Mutation("createActivity")
    public ActivityResponse createActivity(ActivityInput input) {
        Activity activity = dtoMapper.toDomain(input);
        Activity created = createActivityUseCase.execute(activity);
        return dtoMapper.toResponse(created);
    }

    @Mutation("updateActivity")
    public ActivityResponse updateActivity(ActivityUpdateInput input) {
        Activity activity = dtoMapper.toDomain(input);
        Activity updated = updateActivityUseCase.execute(activity);
        return dtoMapper.toResponse(updated);
    }

    @Mutation("deleteActivity")
    public Boolean deleteActivity(Long id) {
        deleteActivityUseCase.execute(id);
        return true;
    }
}