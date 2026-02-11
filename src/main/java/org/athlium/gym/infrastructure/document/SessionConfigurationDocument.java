package org.athlium.gym.infrastructure.document;

import org.athlium.gym.domain.model.WaitlistStrategy;

import java.time.Instant;

public class SessionConfigurationDocument {

    public Integer maxParticipants;
    public Boolean waitlistEnabled;
    public Integer waitlistMaxSize;
    public WaitlistStrategy waitlistStrategy;
    public Integer cancellationMinHoursBeforeStart;
    public Boolean cancellationAllowLateCancel;
    public Instant updatedAt;
}
