package org.athlium.gym.application.usecase.template;

import org.athlium.gym.domain.model.ActivitySchedule;

import java.time.LocalDate;
import java.util.List;

public interface SessionTemplateBuilder {

    boolean supports(ActivitySchedule schedule);

    List<SessionSlot> buildSlots(ActivitySchedule schedule, LocalDate weekStart);
}
