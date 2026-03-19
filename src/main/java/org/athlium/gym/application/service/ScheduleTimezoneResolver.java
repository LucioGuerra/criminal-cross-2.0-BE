package org.athlium.gym.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.ZoneId;

@ApplicationScoped
public class ScheduleTimezoneResolver {

    private static final String FALLBACK_ZONE = "America/Argentina/Buenos_Aires";

    @ConfigProperty(name = "athlium.schedule.timezone.default", defaultValue = FALLBACK_ZONE)
    String configuredDefaultZone;

    public ZoneId resolveForHeadquarters(Long headquartersId) {
        return resolveConfiguredZone();
    }

    private ZoneId resolveConfiguredZone() {
        try {
            return ZoneId.of(configuredDefaultZone);
        } catch (Exception ignored) {
            return ZoneId.of(FALLBACK_ZONE);
        }
    }
}
