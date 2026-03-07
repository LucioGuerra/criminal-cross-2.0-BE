package org.athlium.gym.presentation.mapper;

import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.model.SessionInstance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        var response = mapper.toResponse(session);

        assertEquals(11L, response.getId());
        assertEquals(4L, response.getActivity().getId());
        assertEquals("Spinning", response.getActivity().getName());
    }
}
