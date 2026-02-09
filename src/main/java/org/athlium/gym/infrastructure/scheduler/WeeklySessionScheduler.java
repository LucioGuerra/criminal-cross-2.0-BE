package org.athlium.gym.infrastructure.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.application.usecase.GenerateNextWeekSessionsUseCase;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WeeklySessionScheduler {

    private static final Logger LOG = Logger.getLogger(WeeklySessionScheduler.class);

    @Inject
    GenerateNextWeekSessionsUseCase generateNextWeekSessionsUseCase;

    @Scheduled(cron = "0 0 3 ? * MON")
    void generateWeeklySessions() {
        var result = generateNextWeekSessionsUseCase.execute();
        LOG.infof("Weekly session generation completed: created=%d skipped=%d", result.created(), result.skipped());
    }
}
