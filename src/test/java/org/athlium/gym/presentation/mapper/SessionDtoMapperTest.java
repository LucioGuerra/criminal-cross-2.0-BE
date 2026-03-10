package org.athlium.gym.presentation.mapper;

import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionParticipant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

class SessionDtoMapperTest {

    private final SessionDtoMapper mapper = new SessionDtoMapperImpl();

    @Test
    void shouldMapNestedActivity() {
        Activity activity = Activity.builder()
                .id(4L)
                .name("Spinning")
                .description("Indoor cycling")
                .isActive(true)
                .hqId(2L)
                .build();

        SessionInstance session = new SessionInstance();
        session.setId(11L);
        session.setActivityId(4L);
        session.setActivity(activity);
        SessionParticipant participant = new SessionParticipant();
        participant.setId(9L);
        participant.setName("Jane");
        participant.setLastName("Roe");
        participant.setEmail("jane@test.com");
        session.setParticipants(List.of(participant));

        var response = mapper.toResponse(session);

        assertEquals(11L, response.getId());
        assertEquals(4L, response.getActivity().getId());
        assertEquals("Spinning", response.getActivity().getName());
        assertEquals(9L, response.getParticipants().getFirst().getId());
        assertEquals("Jane", response.getParticipants().getFirst().getName());
    }
}
