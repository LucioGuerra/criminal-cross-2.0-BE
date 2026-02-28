package org.athlium.gym.domain.model;

import java.time.Instant;

public class SessionInstance {

    private Long id;
    private Long organizationId;
    private Long headquartersId;
    private Long activityId;
    private Instant startsAt;
    private Instant endsAt;
    private SessionStatus status;
    private SessionSource source;
    private Integer maxParticipants;
    private Boolean waitlistEnabled;
    private Integer waitlistMaxSize;
    private WaitlistStrategy waitlistStrategy;
    private Integer cancellationMinHoursBeforeStart;
    private Boolean cancellationAllowLateCancel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getHeadquartersId() {
        return headquartersId;
    }

    public void setHeadquartersId(Long headquartersId) {
        this.headquartersId = headquartersId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Instant startsAt) {
        this.startsAt = startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public SessionSource getSource() {
        return source;
    }

    public void setSource(SessionSource source) {
        this.source = source;
    }

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
