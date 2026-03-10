package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.gym.domain.repository.SessionParticipantRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

import java.util.List;

@ApplicationScoped
public class GetSessionByIdUseCase {

    @Inject
    SessionInstanceRepository sessionInstanceRepository;

    @Inject
    ActivityRepository activityRepository;

    @Inject
    SessionParticipantRepository sessionParticipantRepository;

    public SessionInstance execute(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Session ID must be a positive number");
        }

        SessionInstance session = sessionInstanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Session", id));

        if (session.getActivityId() != null && session.getActivityId() > 0) {
            session.setActivity(activityRepository.findByIds(List.of(session.getActivityId()))
                    .get(session.getActivityId()));
        }

        session.setParticipants(
                sessionParticipantRepository.findBySessionIds(List.of(session.getId()))
                        .getOrDefault(session.getId(), List.of())
        );

        return session;
    }
}
