package org.athlium.gym.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.infrastructure.entity.BookingEntity;
import org.athlium.gym.domain.model.SessionParticipant;
import org.athlium.gym.domain.repository.SessionParticipantRepository;
import org.athlium.users.infrastructure.entity.UserEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SessionParticipantRepositoryImpl implements SessionParticipantRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public Map<Long, List<SessionParticipant>> findBySessionIds(List<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = entityManager.createQuery(
                        """
                        select b.sessionId, u.id, u.name, u.lastName, u.email
                        from BookingEntity b
                        join UserEntity u on u.id = b.userId
                        where b.sessionId in :sessionIds and b.status <> :cancelledStatus
                        order by b.sessionId asc, b.createdAt asc
                        """,
                        Object[].class
                )
                .setParameter("sessionIds", sessionIds)
                .setParameter("cancelledStatus", BookingStatus.CANCELLED)
                .getResultList();

        Map<Long, Map<Long, SessionParticipant>> participantsBySession = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long sessionId = (Long) row[0];
            Long participantId = (Long) row[1];

            participantsBySession.computeIfAbsent(sessionId, ignored -> new LinkedHashMap<>());
            participantsBySession.get(sessionId).computeIfAbsent(participantId, ignored -> {
                SessionParticipant participant = new SessionParticipant();
                participant.setId(participantId);
                participant.setName((String) row[2]);
                participant.setLastName((String) row[3]);
                participant.setEmail((String) row[4]);
                return participant;
            });
        }

        Map<Long, List<SessionParticipant>> response = new LinkedHashMap<>();
        participantsBySession.forEach((sessionId, participants) ->
                response.put(sessionId, new ArrayList<>(participants.values()))
        );
        return response;
    }
}
