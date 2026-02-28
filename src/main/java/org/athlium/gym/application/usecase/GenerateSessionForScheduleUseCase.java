package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SchedulerType;
import org.athlium.gym.domain.model.SessionConfiguration;
import org.athlium.gym.application.usecase.template.SessionTemplateDirector;
import org.athlium.gym.application.usecase.template.SessionSlot;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class GenerateSessionForScheduleUseCase {

    @Inject
    PersistGeneratedSessionUseCase persistGeneratedSessionUseCase;

    @Inject
    ResolveSessionConfigurationUseCase resolveSessionConfigurationUseCase;

    @Inject
    SessionTemplateDirector sessionTemplateDirector;

    @Inject
    ActivityScheduleRepository activityScheduleRepository;

    public GenerationResult execute(ActivitySchedule schedule, LocalDate nextMonday) {
        List<SessionSlot> slots = sessionTemplateDirector.buildSlotsForWeek(schedule, nextMonday);
        if (slots.isEmpty()) {
            return new GenerationResult(0, 0, false);
        }

        SessionConfiguration config = resolveSessionConfigurationUseCase.execute(
                schedule.getOrganizationId(),
                schedule.getHeadquartersId(),
                schedule.getActivityId(),
                null
        );

        int created = 0;
        int skipped = 0;
        for (var slot : slots) {
            GenerationStatus status = persistGeneratedSessionUseCase.execute(schedule, slot.startsAt(), slot.endsAt(), config);
            if (status == GenerationStatus.CREATED) {
                created++;
            } else if (status == GenerationStatus.SKIPPED) {
                skipped++;
            }
        }

        boolean shouldDeactivate = schedule.getSchedulerType() == SchedulerType.ONE_TIME_DISPOSABLE && (created > 0 || skipped > 0);
        if (shouldDeactivate) {
            schedule.setActive(false);
            activityScheduleRepository.save(schedule);
        }

        return new GenerationResult(created, skipped, shouldDeactivate);
    }

    public enum GenerationStatus {
        CREATED,
        SKIPPED,
        NOT_APPLICABLE
    }

    public record GenerationResult(int created, int skipped, boolean deactivated) {
    }
}
