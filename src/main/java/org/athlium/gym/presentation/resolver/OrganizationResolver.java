package org.athlium.gym.presentation.resolver;

import jakarta.inject.Inject;
import org.athlium.gym.application.usecase.GetHeadquartersByOrganizationUseCase;
import org.athlium.gym.presentation.dto.HeadquartersResponse;
import org.athlium.gym.presentation.dto.OrganizationResponse;
import org.athlium.gym.presentation.mapper.HeadquartersDtoMapper;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;
import java.util.stream.Collectors;

@GraphQLApi
public class OrganizationResolver {

    @Inject
    GetHeadquartersByOrganizationUseCase getHeadquartersByOrganizationUseCase;

    @Inject
    HeadquartersDtoMapper headquartersMapper;

    public List<HeadquartersResponse> headquarters(@Source OrganizationResponse organization) {
        return getHeadquartersByOrganizationUseCase.execute(organization.getId()).stream()
                .map(headquartersMapper::toResponse)
                .collect(Collectors.toList());
    }
}