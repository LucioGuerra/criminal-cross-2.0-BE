package org.athlium.gym.presentation.controller;

import jakarta.inject.Inject;
import org.athlium.gym.application.usecase.*;
import org.athlium.gym.presentation.dto.HeadquartersInput;
import org.athlium.gym.presentation.dto.HeadquartersResponse;
import org.athlium.gym.presentation.mapper.HeadquartersDtoMapper;
import org.eclipse.microprofile.graphql.*;

import java.util.List;
import java.util.stream.Collectors;

@GraphQLApi
public class HeadquartersGraphQLResource {

    @Inject
    CreateHeadquartersUseCase createHeadquartersUseCase;

    @Inject
    GetHeadquartersUseCase getHeadquartersUseCase;

    @Inject
    GetAllHeadquartersUseCase getAllHeadquartersUseCase;

    @Inject
    GetHeadquartersByOrganizationUseCase getHeadquartersByOrganizationUseCase;

    @Inject
    UpdateHeadquartersUseCase updateHeadquartersUseCase;

    @Inject
    DeleteHeadquartersUseCase deleteHeadquartersUseCase;

    @Inject
    HeadquartersDtoMapper mapper;

    @Query("headquarters")
    @Description("Get all headquarters")
    public List<HeadquartersResponse> getHeadquarters() {
        return getAllHeadquartersUseCase.execute().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Query("headquarter")
    @Description("Get headquarters by ID")
    public HeadquartersResponse getHeadquarters(@Name("id") Long id) {
        return mapper.toResponse(getHeadquartersUseCase.execute(id));
    }

    @Query("headquartersByOrganization")
    @Description("Get headquarters by organization ID")
    public List<HeadquartersResponse> getHeadquartersByOrganization(@Name("organizationId") Long organizationId) {
        return getHeadquartersByOrganizationUseCase.execute(organizationId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Mutation("createHeadquarters")
    @Description("Create new headquarters")
    public HeadquartersResponse createHeadquarters(@Name("input") HeadquartersInput input) {
        var domain = mapper.toDomain(input);
        var created = createHeadquartersUseCase.execute(domain);
        return mapper.toResponse(created);
    }

    @Mutation("updateHeadquarters")
    @Description("Update existing headquarters")
    public HeadquartersResponse updateHeadquarters(@Name("id") Long id, @Name("input") HeadquartersInput input) {
        var domain = mapper.toDomain(input);
        var updated = updateHeadquartersUseCase.execute(id, domain);
        return mapper.toResponse(updated);
    }

    @Mutation("deleteHeadquarters")
    @Description("Delete headquarters")
    public Boolean deleteHeadquarters(@Name("id") Long id) {
        deleteHeadquartersUseCase.execute(id);
        return true;
    }
}