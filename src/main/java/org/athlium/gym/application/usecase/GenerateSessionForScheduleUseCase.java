package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SessionConfiguration;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class GenerateSessionForScheduleUseCase {

    @Inject
    PersistGeneratedSessionUseCase persistGeneratedSessionUseCase;

    @Inject
    ResolveSessionConfigurationUseCase resolveSessionConfigurationUseCase;

    public GenerationStatus execute(ActivitySchedule schedule, LocalDate nextMonday) {
        LocalDate sessionDate = nextMonday.plusDays(schedule.getDayOfWeek() - 1L);
        LocalDateTime startsAtLocal = LocalDateTime.of(sessionDate, schedule.getStartTime());
        Instant startsAt = startsAtLocal.toInstant(ZoneOffset.UTC);
        Instant endsAt = startsAt.plusSeconds(schedule.getDurationMinutes() * 60L);

        SessionConfiguration config = resolveSessionConfigurationUseCase.execute(
                schedule.getOrganizationId(),
                schedule.getHeadquartersId(),
                schedule.getActivityId(),
                null
        );

        return persistGeneratedSessionUseCase.execute(schedule, startsAt, endsAt, config);
    }

    public enum GenerationStatus {
        CREATED,
        SKIPPED
    }
}
