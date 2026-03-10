package org.athlium.gym.domain.repository;

import org.athlium.gym.domain.model.SessionParticipant;

import java.util.List;
import java.util.Map;

public interface SessionParticipantRepository {

    Map<Long, List<SessionParticipant>> findBySessionIds(List<Long> sessionIds);
}
