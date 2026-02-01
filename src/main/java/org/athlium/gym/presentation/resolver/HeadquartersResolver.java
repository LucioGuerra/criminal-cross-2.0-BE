package org.athlium.gym.presentation.resolver;

import jakarta.inject.Inject;
import org.athlium.gym.application.usecase.GetOrganizationUseCase;
import org.athlium.gym.presentation.dto.HeadquartersResponse;
import org.athlium.gym.presentation.dto.OrganizationResponse;
import org.athlium.gym.presentation.mapper.OrganizationDtoMapper;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class HeadquartersResolver {

    @Inject
    GetOrganizationUseCase getOrganizationUseCase;

    @Inject
    OrganizationDtoMapper organizationMapper;

    public OrganizationResponse organization(@Source HeadquartersResponse headquarters) {
        var organization = getOrganizationUseCase.execute(headquarters.getOrganizationId());
        return organizationMapper.toResponse(organization);
    }
}