package org.athlium.gym.application.usecase;

import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.model.WaitlistStrategy;
import org.athlium.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionConfigurationValidatorTest {

    @Test
    void shouldRejectInvalidMaxParticipants() {
        SessionConfiguration config = new SessionConfiguration();
        config.setMaxParticipants(0);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> SessionConfigurationValidator.validate(config));

        assertEquals("maxParticipants must be greater than 0", ex.getMessage());
    }

    @Test
    void shouldRejectWaitlistEnabledWithZeroMaxSize() {
        SessionConfiguration config = new SessionConfiguration();
        config.setWaitlistEnabled(true);
        config.setWaitlistMaxSize(0);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> SessionConfigurationValidator.validate(config));

        assertEquals("waitlistMaxSize must be greater than 0 when waitlist is enabled", ex.getMessage());
    }

    @Test
    void shouldSetDefaultWaitlistStrategyWhenEnabled() {
        SessionConfiguration config = new SessionConfiguration();
        config.setWaitlistEnabled(true);
        config.setWaitlistMaxSize(3);

        SessionConfigurationValidator.validate(config);

        assertEquals(WaitlistStrategy.FIFO, config.getWaitlistStrategy());
    }
}
