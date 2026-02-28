package org.athlium.gym.application.usecase.template;

import java.time.Instant;

public record SessionSlot(Instant startsAt, Instant endsAt) {
}
