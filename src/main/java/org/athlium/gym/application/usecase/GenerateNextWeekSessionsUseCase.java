package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.model.SchedulerType;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.jboss.logging.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GenerateNextWeekSessionsUseCase {

    private static final Logger LOG = Logger.getLogger(GenerateNextWeekSessionsUseCase.class);

    @Inject
    ActivityScheduleRepository activityScheduleRepository;

    @Inject
    GenerateSessionForScheduleUseCase generateSessionForScheduleUseCase;

    public GenerationResult execute() {
        List<ActivitySchedule> schedules = activityScheduleRepository.findAllActive();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate nextMonday = LocalDate.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        int created = 0;
        int skipped = 0;
        int failed = 0;
        int deactivated = 0;

        for (ActivitySchedule schedule : new ArrayList<>(schedules)) {
            try {
                if (shouldDeactivateByExpiration(schedule, today)) {
                    schedule.setActive(false);
                    activityScheduleRepository.save(schedule);
                    deactivated++;
                    continue;
                }

                var result = generateSessionForScheduleUseCase.execute(schedule, nextMonday);
                created += result.created();
                skipped += result.skipped();
                if (result.deactivated()) {
                    deactivated++;
                }
            } catch (Exception ex) {
                failed++;
                LOG.errorf(
                        ex,
                        "Failed generating session for schedule id=%s organizationId=%s headquartersId=%s activityId=%s",
                        schedule.getId(),
                        schedule.getOrganizationId(),
                        schedule.getHeadquartersId(),
                        schedule.getActivityId()
                );
            }
        }

        return new GenerationResult(created, skipped, failed, deactivated);
    }

    private boolean shouldDeactivateByExpiration(ActivitySchedule schedule, LocalDate today) {
        return schedule.getSchedulerType() == SchedulerType.WEEKLY_RANGE
                && schedule.getActiveUntil() != null
                && today.isAfter(schedule.getActiveUntil());
    }

    public record GenerationResult(int created, int skipped, int failed, int deactivated) {
    }
}
