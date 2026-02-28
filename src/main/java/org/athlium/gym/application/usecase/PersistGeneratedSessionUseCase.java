package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionSource;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.hibernate.exception.ConstraintViolationException;

import java.time.Instant;

@ApplicationScoped
public class PersistGeneratedSessionUseCase {

    @Inject
    SessionInstanceRepository sessionInstanceRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public GenerateSessionForScheduleUseCase.GenerationStatus execute(
            ActivitySchedule schedule,
            Instant startsAt,
            Instant endsAt,
            SessionConfiguration config
    ) {
        boolean exists = sessionInstanceRepository.existsByOrganizationAndHeadquartersAndActivityAndStartsAt(
                schedule.getOrganizationId(),
                schedule.getHeadquartersId(),
                schedule.getActivityId(),
                startsAt
        );

        if (exists) {
            return GenerateSessionForScheduleUseCase.GenerationStatus.SKIPPED;
        }

        SessionInstance session = new SessionInstance();
        session.setOrganizationId(schedule.getOrganizationId());
        session.setHeadquartersId(schedule.getHeadquartersId());
        session.setActivityId(schedule.getActivityId());
        session.setStartsAt(startsAt);
        session.setEndsAt(endsAt);
        session.setStatus(SessionStatus.OPEN);
        session.setSource(SessionSource.SCHEDULER);
        session.setMaxParticipants(config.getMaxParticipants());
        session.setWaitlistEnabled(config.getWaitlistEnabled());
        session.setWaitlistMaxSize(config.getWaitlistMaxSize());
        session.setWaitlistStrategy(config.getWaitlistStrategy());
        session.setCancellationMinHoursBeforeStart(config.getCancellationMinHoursBeforeStart());
        session.setCancellationAllowLateCancel(config.getCancellationAllowLateCancel());

        try {
            sessionInstanceRepository.save(session);
            return GenerateSessionForScheduleUseCase.GenerationStatus.CREATED;
        } catch (RuntimeException ex) {
            if (isUniqueConstraintViolation(ex)) {
                return GenerateSessionForScheduleUseCase.GenerationStatus.SKIPPED;
            }
            throw ex;
        }
    }

    private boolean isUniqueConstraintViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConstraintViolationException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
