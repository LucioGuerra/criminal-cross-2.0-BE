package org.athlium.gym.presentation.dto;

import org.athlium.gym.domain.model.WaitlistStrategy;

public class SessionConfigurationRequest {

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
}
