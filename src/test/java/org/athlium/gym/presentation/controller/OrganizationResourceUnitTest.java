package org.athlium.gym.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import org.athlium.gym.application.usecase.GetOrganizationUseCase;
import org.athlium.gym.application.usecase.GetOrganizationsUseCase;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.presentation.dto.OrganizationResponse;
import org.athlium.gym.presentation.mapper.OrganizationDtoMapperImpl;
import org.athlium.shared.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganizationResourceUnitTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private OrganizationResource resource;
    private StubGetOrganizationsUseCase getOrganizationsUseCase;
    private StubGetOrganizationUseCase getOrganizationUseCase;

    @BeforeEach
    void setUp() {
        resource = new OrganizationResource();

        getOrganizationsUseCase = new StubGetOrganizationsUseCase();
        getOrganizationUseCase = new StubGetOrganizationUseCase();

        resource.getOrganizationsUseCase = getOrganizationsUseCase;
        resource.getOrganizationUseCase = getOrganizationUseCase;
        resource.mapper = new OrganizationDtoMapperImpl();
    }

    @Test
    void shouldPopulateHeadquartersInOrganizationsListResponse() {
        Headquarters hq = Headquarters.builder().id(100L).name("HQ Centro").organizationId(10L).build();
        Organization organization = Organization.builder()
                .id(10L)
                .name("Org Test")
                .headQuarters(List.of(hq))
                .build();
        getOrganizationsUseCase.response = List.of(organization);

        Response response = resource.getOrganizations();

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        List<?> data = (List<?>) body.getData();
        OrganizationResponse item = (OrganizationResponse) data.getFirst();

        assertNotNull(item.getHeadquarters());
        assertEquals(1, item.getHeadquarters().size());
        assertEquals(100L, item.getHeadquarters().getFirst().getId());
        assertEquals("HQ Centro", item.getHeadquarters().getFirst().getName());

        Map<?, ?> headquarterAsMap = OBJECT_MAPPER.convertValue(item.getHeadquarters().getFirst(), Map.class);
        assertEquals(2, headquarterAsMap.size());
        assertTrue(headquarterAsMap.containsKey("id"));
        assertTrue(headquarterAsMap.containsKey("name"));
    }

    @Test
    void shouldPopulateHeadquartersInOrganizationByIdResponse() {
        Headquarters hq = Headquarters.builder().id(101L).name("HQ Norte").organizationId(11L).build();
        Organization organization = Organization.builder()
                .id(11L)
                .name("Org Detail")
                .headQuarters(List.of(hq))
                .build();
        getOrganizationUseCase.response = organization;

        Response response = resource.getOrganization(11L);

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        OrganizationResponse data = (OrganizationResponse) body.getData();

        assertNotNull(data.getHeadquarters());
        assertEquals(1, data.getHeadquarters().size());
        assertEquals(101L, data.getHeadquarters().getFirst().getId());
        assertEquals("HQ Norte", data.getHeadquarters().getFirst().getName());

        Map<?, ?> headquarterAsMap = OBJECT_MAPPER.convertValue(data.getHeadquarters().getFirst(), Map.class);
        assertEquals(2, headquarterAsMap.size());
        assertTrue(headquarterAsMap.containsKey("id"));
        assertTrue(headquarterAsMap.containsKey("name"));
    }

    private static class StubGetOrganizationsUseCase extends GetOrganizationsUseCase {
        List<Organization> response = List.of();

        @Override
        public List<Organization> execute() {
            return response;
        }
    }

    private static class StubGetOrganizationUseCase extends GetOrganizationUseCase {
        Organization response;

        @Override
        public Organization execute(Long id) {
            return response;
        }
    }
}
