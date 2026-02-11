package org.athlium.gym.domain.model;

public class SessionConfiguration {

    private Integer maxParticipants;
    private Boolean waitlistEnabled;
    private Integer waitlistMaxSize;
    private WaitlistStrategy waitlistStrategy;
    private Integer cancellationMinHoursBeforeStart;
    private Boolean cancellationAllowLateCancel;

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Boolean getWaitlistEnabled() {
        return waitlistEnabled;
    }

    public void setWaitlistEnabled(Boolean waitlistEnabled) {
        this.waitlistEnabled = waitlistEnabled;
    }

    public Integer getWaitlistMaxSize() {
        return waitlistMaxSize;
    }

    public void setWaitlistMaxSize(Integer waitlistMaxSize) {
        this.waitlistMaxSize = waitlistMaxSize;
    }

    public WaitlistStrategy getWaitlistStrategy() {
        return waitlistStrategy;
    }

    public void setWaitlistStrategy(WaitlistStrategy waitlistStrategy) {
        this.waitlistStrategy = waitlistStrategy;
    }

    public Integer getCancellationMinHoursBeforeStart() {
        return cancellationMinHoursBeforeStart;
    }

    public void setCancellationMinHoursBeforeStart(Integer cancellationMinHoursBeforeStart) {
        this.cancellationMinHoursBeforeStart = cancellationMinHoursBeforeStart;
    }

    public Boolean getCancellationAllowLateCancel() {
        return cancellationAllowLateCancel;
    }

    public void setCancellationAllowLateCancel(Boolean cancellationAllowLateCancel) {
        this.cancellationAllowLateCancel = cancellationAllowLateCancel;
    }

    public SessionConfiguration mergeWith(SessionConfiguration override) {
        if (override == null) {
            return this.copy();
        }

        SessionConfiguration merged = this.copy();
        if (override.maxParticipants != null) {
            merged.maxParticipants = override.maxParticipants;
        }
        if (override.waitlistEnabled != null) {
            merged.waitlistEnabled = override.waitlistEnabled;
        }
        if (override.waitlistMaxSize != null) {
            merged.waitlistMaxSize = override.waitlistMaxSize;
        }
        if (override.waitlistStrategy != null) {
            merged.waitlistStrategy = override.waitlistStrategy;
        }
        if (override.cancellationMinHoursBeforeStart != null) {
            merged.cancellationMinHoursBeforeStart = override.cancellationMinHoursBeforeStart;
        }
        if (override.cancellationAllowLateCancel != null) {
            merged.cancellationAllowLateCancel = override.cancellationAllowLateCancel;
        }
        return merged;
    }

    public SessionConfiguration copy() {
        SessionConfiguration copy = new SessionConfiguration();
        copy.maxParticipants = this.maxParticipants;
        copy.waitlistEnabled = this.waitlistEnabled;
        copy.waitlistMaxSize = this.waitlistMaxSize;
        copy.waitlistStrategy = this.waitlistStrategy;
        copy.cancellationMinHoursBeforeStart = this.cancellationMinHoursBeforeStart;
        copy.cancellationAllowLateCancel = this.cancellationAllowLateCancel;
        return copy;
    }

    public static SessionConfiguration defaults() {
        SessionConfiguration defaults = new SessionConfiguration();
        defaults.maxParticipants = 1;
        defaults.waitlistEnabled = false;
        defaults.waitlistMaxSize = 0;
        defaults.waitlistStrategy = WaitlistStrategy.FIFO;
        defaults.cancellationMinHoursBeforeStart = 0;
        defaults.cancellationAllowLateCancel = true;
        return defaults;
    }
}
