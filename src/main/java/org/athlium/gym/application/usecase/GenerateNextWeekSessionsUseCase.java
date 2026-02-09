package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionSource;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.athlium.gym.domain.repository.SessionInstanceRepository;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@ApplicationScoped
public class GenerateNextWeekSessionsUseCase {

    @Inject
    ActivityScheduleRepository activityScheduleRepository;

    @Inject
    SessionInstanceRepository sessionInstanceRepository;

    @Inject
    ResolveSessionConfigurationUseCase resolveSessionConfigurationUseCase;

    public GenerationResult execute() {
        List<ActivitySchedule> schedules = activityScheduleRepository.findAllActive();
        LocalDate nextMonday = LocalDate.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        int created = 0;
        int skipped = 0;

        for (ActivitySchedule schedule : schedules) {
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
                skipped++;
                continue;
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
            created++;
        }

        return new GenerationResult(created, skipped);
    }

    public record GenerationResult(int created, int skipped) {
    }
}
