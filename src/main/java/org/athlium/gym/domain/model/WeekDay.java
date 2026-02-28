package org.athlium.gym.domain.model;

public enum WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public int toIsoDayValue() {
        return ordinal() + 1;
    }

    public static WeekDay fromIsoDayValue(int value) {
        if (value < 1 || value > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
        return values()[value - 1];
    }
}
