package org.athlium.users.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class UserWithPackageStatus {

    private Long id;
    private String name;
    private String lastName;
    private String email;
    private Set<Role> roles;
    private Boolean active;
    private PackageStatus packageStatus;
    private LocalDate periodEnd;
    private Integer daysRemaining;
    private List<UserHqMembership> hqMemberships;

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public PackageStatus getPackageStatus() {
        return packageStatus;
    }

    public void setPackageStatus(PackageStatus packageStatus) {
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

    public List<UserHqMembership> getHqMemberships() {
        return hqMemberships;
    }

    public void setHqMemberships(List<UserHqMembership> hqMemberships) {
        this.hqMemberships = hqMemberships;
    }
}
