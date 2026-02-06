package org.athlium.gym.presentation.dto;

import lombok.Data;
import org.athlium.gym.domain.model.SessionSource;
import org.athlium.gym.domain.model.SessionStatus;

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
}
