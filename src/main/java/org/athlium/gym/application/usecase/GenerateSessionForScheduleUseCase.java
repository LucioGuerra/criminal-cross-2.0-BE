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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class GenerateSessionForScheduleUseCase {

    @Inject
    SessionInstanceRepository sessionInstanceRepository;

    @Inject
    ResolveSessionConfigurationUseCase resolveSessionConfigurationUseCase;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public GenerationStatus execute(ActivitySchedule schedule, LocalDate nextMonday) {
        LocalDate sessionDate = nextMonday.plusDays(schedule.getDayOfWeek() - 1L);
        LocalDateTime startsAtLocal = LocalDateTime.of(sessionDate, schedule.getStartTime());
        Instant startsAt = startsAtLocal.toInstant(ZoneOffset.UTC);
        Instant endsAt = startsAt.plusSeconds(schedule.getDurationMinutes() * 60L);

        boolean exists = sessionInstanceRepository.existsByOrganizationAndHeadquartersAndActivityAndStartsAt(
                schedule.getOrganizationId(),
                schedule.getHeadquartersId(),
                schedule.getActivityId(),
                startsAt
        );

        if (exists) {
            return GenerationStatus.SKIPPED;
        }

        SessionConfiguration config = resolveSessionConfigurationUseCase.execute(
                schedule.getOrganizationId(),
                schedule.getHeadquartersId(),
                schedule.getActivityId(),
                null
        );

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

        sessionInstanceRepository.save(session);
        return GenerationStatus.CREATED;
    }

    public enum GenerationStatus {
        CREATED,
        SKIPPED
    }
}
