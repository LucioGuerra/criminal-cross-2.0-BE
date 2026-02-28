package org.athlium.gym.application.usecase.template;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.ActivitySchedule;
import org.athlium.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class SessionTemplateDirector {

    @Inject
    Instance<SessionTemplateBuilder> cdiBuilders;

    private Iterable<SessionTemplateBuilder> buildersOverride;

    public void setBuildersForTesting(Iterable<SessionTemplateBuilder> buildersOverride) {
        this.buildersOverride = buildersOverride;
    }

    public List<SessionSlot> buildSlotsForWeek(ActivitySchedule schedule, LocalDate weekStart) {
        Iterable<SessionTemplateBuilder> builders = buildersOverride != null ? buildersOverride : cdiBuilders;
        for (SessionTemplateBuilder builder : builders) {
            if (builder.supports(schedule)) {
                return builder.buildSlots(schedule, weekStart);
            }
        }

        throw new BadRequestException("No session template builder found for type " + schedule.getTemplateType());
    }
}
