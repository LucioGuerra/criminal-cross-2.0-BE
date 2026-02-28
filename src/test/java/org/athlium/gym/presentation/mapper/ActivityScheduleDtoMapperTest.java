package org.athlium.gym.presentation.mapper;

import org.athlium.gym.domain.model.SchedulerType;
import org.athlium.gym.domain.model.WeekDay;
import org.athlium.gym.presentation.dto.ActivityScheduleRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActivityScheduleDtoMapperTest {

    private final ActivityScheduleDtoMapper mapper = new ActivityScheduleDtoMapper();

    @Test
    void shouldMapEnumWeekDaysFromRequest() {
        ActivityScheduleRequest request = new ActivityScheduleRequest();
        request.setOrganizationId(1L);
        request.setHeadquartersId(2L);
        request.setActivityId(3L);
        request.setStartTime("18:30");
        request.setDurationMinutes(60);
        request.setSchedulerType("weekly_range");
        request.setWeekDays(List.of("monday", "wednesday"));

        var schedule = mapper.toDomain(request);

        assertEquals(SchedulerType.WEEKLY_RANGE, schedule.getSchedulerType());
        assertEquals(List.of(WeekDay.MONDAY, WeekDay.WEDNESDAY), schedule.getWeekDays());
    }

    @Test
    void shouldMapLegacyTemplateTypeAndNumericDayOfWeek() {
        ActivityScheduleRequest request = new ActivityScheduleRequest();
        request.setOrganizationId(1L);
        request.setHeadquartersId(2L);
        request.setActivityId(3L);
        request.setStartTime("18:30");
        request.setDurationMinutes(60);
        request.setTemplateType("weekly_range");
        request.setDayOfWeek(5);

        var schedule = mapper.toDomain(request);

        assertEquals(SchedulerType.WEEKLY_RANGE, schedule.getSchedulerType());
        assertEquals(List.of(WeekDay.FRIDAY), schedule.getWeekDays());
    }
}
