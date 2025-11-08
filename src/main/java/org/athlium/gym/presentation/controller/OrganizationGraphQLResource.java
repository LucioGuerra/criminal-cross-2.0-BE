package org.athlium.gym.presentation.controller;

import jakarta.inject.Inject;
import org.athlium.gym.application.usecase.*;
import org.athlium.gym.presentation.dto.OrganizationInput;
import org.athlium.gym.presentation.dto.OrganizationResponse;
import org.athlium.gym.presentation.mapper.OrganizationDtoMapper;
import org.eclipse.microprofile.graphql.*;

import java.util.List;
import java.util.stream.Collectors;

@GraphQLApi
public class OrganizationGraphQLResource {

    @Inject
    CreateOrganizationUseCase createOrganizationUseCase;

    @Inject
    GetOrganizationUseCase getOrganizationUseCase;

    @Inject
    GetOrganizationsUseCase getOrganizationsUseCase;

    @Inject
    UpdateOrganizationUseCase updateOrganizationUseCase;

    @Inject
    DeleteOrganizationUseCase deleteOrganizationUseCase;

    @Inject
    OrganizationDtoMapper mapper;

    @Query("organizations")
    @Description("Get all organizations")
    public List<OrganizationResponse> getOrganizations() {
        return getOrganizationsUseCase.execute().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Query("organization")
    @Description("Get organization by ID")
    public OrganizationResponse getOrganization(@Name("id") Long id) {
        return mapper.toResponse(getOrganizationUseCase.execute(id));
    }

    @Mutation("createOrganization")
    @Description("Create a new organization")
    public OrganizationResponse createOrganization(@Name("input") OrganizationInput input) {
        var domain = mapper.toDomain(input);
        var created = createOrganizationUseCase.execute(domain);
        return mapper.toResponse(created);
    }

    @Mutation("updateOrganization")
    @Description("Update an existing organization")
    public OrganizationResponse updateOrganization(@Name("id") Long id, @Name("input") OrganizationInput input) {
        var domain = mapper.toDomain(input);
        var updated = updateOrganizationUseCase.execute(id, domain);
        return mapper.toResponse(updated);
    }

    @Mutation("deleteOrganization")
    @Description("Delete an organization")
    public Boolean deleteOrganization(@Name("id") Long id) {
        deleteOrganizationUseCase.execute(id);
        return true;
    }
}