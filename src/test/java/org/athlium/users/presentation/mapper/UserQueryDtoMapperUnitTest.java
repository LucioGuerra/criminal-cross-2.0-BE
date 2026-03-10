package org.athlium.users.presentation.mapper;

import org.athlium.users.domain.model.PackageStatus;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.infrastructure.dto.UserResponseDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserQueryDtoMapperUnitTest {

    private final UserQueryDtoMapper mapper = new UserQueryDtoMapper();

    @Test
    void shouldMapUserWithStatusToUnifiedResponse() {
        UserWithPackageStatus domain = new UserWithPackageStatus();
        domain.setId(77L);
        domain.setName("Ana");
        domain.setLastName("Lopez");
        domain.setEmail("ana@test.com");
        domain.setRoles(EnumSet.of(Role.CLIENT));
        domain.setHeadquartersIds(Set.of(10L, 20L));
        domain.setActive(true);
        domain.setPackageStatus(PackageStatus.ACTIVE);
        domain.setPeriodEnd(LocalDate.of(2026, 3, 20));
        domain.setDaysRemaining(10);

        UserResponseDto response = mapper.toResponse(domain);

        assertNotNull(response);
        assertEquals(77L, response.getId());
        assertEquals("Ana", response.getName());
        assertEquals("Lopez", response.getLastName());
        assertEquals("ana@test.com", response.getEmail());
        assertEquals(EnumSet.of(Role.CLIENT), response.getRoles());
        assertEquals(Set.of(10L, 20L), response.getHeadquartersIds());
        assertEquals(true, response.getActive());
        assertEquals("ACTIVE", response.getPackageStatus());
        assertEquals(LocalDate.of(2026, 3, 20), response.getPeriodEnd());
        assertEquals(10, response.getDaysRemaining());
    }

    @Test
    void shouldMapListToUnifiedResponseList() {
        UserWithPackageStatus domain = new UserWithPackageStatus();
        domain.setId(1L);
        domain.setName("Bob");

        List<UserResponseDto> responseList = mapper.toResponseList(List.of(domain));

        assertNotNull(responseList);
        assertEquals(1, responseList.size());
        assertEquals(1L, responseList.get(0).getId());
        assertEquals("Bob", responseList.get(0).getName());
    }
}
