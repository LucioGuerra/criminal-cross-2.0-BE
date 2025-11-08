package org.athlium.gym.presentation.resolver;

import jakarta.inject.Inject;
import org.athlium.gym.application.usecase.GetActivitiesUseCase;
import org.athlium.gym.presentation.dto.ActivityResponse;
import org.athlium.gym.presentation.dto.HeadquartersResponse;
import org.athlium.gym.presentation.mapper.ActivityDtoMapper;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;
import java.util.stream.Collectors;

@GraphQLApi
public class ActivityResolver {

    @Inject
    GetActivitiesUseCase getActivitiesUseCase;

    @Inject
    ActivityDtoMapper activityMapper;

    public List<ActivityResponse> activities(@Source HeadquartersResponse headquarters) {
        return getActivitiesUseCase.executeAllByHeadquarter(headquarters.getId(), true).stream()
                .map(activityMapper::toResponse)
                .collect(Collectors.toList());
    }
}