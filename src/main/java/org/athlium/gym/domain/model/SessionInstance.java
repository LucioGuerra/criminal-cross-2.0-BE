package org.athlium.gym.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionInstance {

    private Long id;
    private Long organizationId;
    private Long headquartersId;
    private Long activityId;
    private Instant startsAt;
    private Instant endsAt;
    private SessionStatus status;
    private SessionSource source;
}
