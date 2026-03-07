package org.athlium.clients.presentation.mapper;

import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.gym.domain.model.Activity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientPackageDtoMapperTest {

    private final ClientPackageDtoMapper mapper = new ClientPackageDtoMapper();

    @Test
    void shouldMapCreditsWithActivityDto() {
        Activity activity = new Activity();
        activity.setId(5L);
        activity.setName("Functional");

        ClientPackageCredit credit = new ClientPackageCredit();
        credit.setActivityId(5L);
        credit.setActivity(activity);
        credit.setTokens(8);

        ClientPackage clientPackage = new ClientPackage();
        clientPackage.setId(1L);
        clientPackage.setUserId(10L);
        clientPackage.setCredits(List.of(credit));

        var response = mapper.toResponse(clientPackage);

        assertEquals(1, response.getCredits().size());
        assertEquals(5L, response.getCredits().getFirst().getActivity().getId());
        assertEquals("Functional", response.getCredits().getFirst().getActivity().getName());
        assertEquals(8, response.getCredits().getFirst().getTokens());
    }
}
