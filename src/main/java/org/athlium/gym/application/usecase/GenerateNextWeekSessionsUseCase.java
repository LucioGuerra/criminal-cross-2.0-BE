package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.gym.domain.repository.ActivityScheduleRepository;
import org.jboss.logging.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
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
        LocalDate nextMonday = LocalDate.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        int created = 0;
        int skipped = 0;
        int failed = 0;

        for (ActivitySchedule schedule : schedules) {
            try {
                var status = generateSessionForScheduleUseCase.execute(schedule, nextMonday);
                if (status == GenerateSessionForScheduleUseCase.GenerationStatus.CREATED) {
                    created++;
                } else {
                    skipped++;
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

        return new GenerationResult(created, skipped, failed);
    }

    public record GenerationResult(int created, int skipped, int failed) {
    }
}
