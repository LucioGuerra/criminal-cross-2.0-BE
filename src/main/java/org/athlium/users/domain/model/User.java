package org.athlium.users.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
@Builder
public class User {

    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String firebaseUid;
    private Set<Role> roles;
    private Boolean active;

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public void addRole(Role role) {
        roles.add(role);
    }


    public void removeRole(Role role) {
        roles.remove(role);
    }


    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void update(String name, String lastName) {
        this.name = name != null && !name.trim().isEmpty() ? name : this.name;
        this.lastName = lastName != null && !lastName.trim().isEmpty() ? lastName : this.lastName;
    }

}
