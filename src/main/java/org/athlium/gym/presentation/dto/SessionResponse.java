package org.athlium.gym.presentation.dto;

import lombok.Data;
import org.athlium.gym.domain.model.SessionSource;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.model.WaitlistStrategy;

import java.time.Instant;

@Data
public class SessionResponse {
    private Long id;
    private Long organizationId;
    private Long headquartersId;
    private Long activityId;
    private Instant startsAt;
    private Instant endsAt;
    private SessionStatus status;
    private SessionSource source;
    private Integer maxParticipants;
    private Boolean waitlistEnabled;
    private Integer waitlistMaxSize;
    private WaitlistStrategy waitlistStrategy;
    private Integer cancellationMinHoursBeforeStart;
    private Boolean cancellationAllowLateCancel;
}
