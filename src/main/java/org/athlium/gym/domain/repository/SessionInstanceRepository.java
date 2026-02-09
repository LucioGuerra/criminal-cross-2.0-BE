package org.athlium.gym.domain.repository;

import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.shared.domain.PageResponse;

import java.time.Instant;
import java.util.Optional;

public interface SessionInstanceRepository {

    SessionInstance save(SessionInstance sessionInstance);

    boolean existsByOrganizationAndHeadquartersAndActivityAndStartsAt(
            Long organizationId,
            Long headquartersId,
            Long activityId,
            Instant startsAt
    );

    Optional<SessionInstance> findById(Long id);

    Optional<SessionInstance> findByIdForUpdate(Long id);

    PageResponse<SessionInstance> findSessions(
            Long organizationId,
            Long headquartersId,
            Long activityId,
            SessionStatus status,
            Instant from,
            Instant to,
            int page,
            int size,
            boolean sortAscending
    );
}
