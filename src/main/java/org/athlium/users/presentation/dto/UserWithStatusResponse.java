package org.athlium.users.presentation.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class UserWithStatusResponse {

    private Long id;
    private String name;
    private String lastName;
    private String email;
    private Set<String> roles;
    private Boolean active;
    private String packageStatus;
    private LocalDate periodEnd;
    private Integer daysRemaining;
    private List<UserHqMembershipResponse> hqMemberships;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPackageStatus() {
        return packageStatus;
    }

    public void setPackageStatus(String packageStatus) {
        this.packageStatus = packageStatus;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Integer getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Integer daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public List<UserHqMembershipResponse> getHqMemberships() {
        return hqMemberships;
    }

    public void setHqMemberships(List<UserHqMembershipResponse> hqMemberships) {
        this.hqMemberships = hqMemberships;
    }
}
