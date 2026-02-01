package org.athlium.auth.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.athlium.users.domain.model.Role;

import java.util.Set;

/**
 * Value object representing an authenticated user session.
 * Contains the essential information extracted from a validated token.
 */
@Getter
@Builder
public class AuthenticatedUser {

    private final String firebaseUid;
    private final String email;
    private final String name;
    private final boolean emailVerified;
    private final AuthProvider provider;
    
    // User data from local database (populated after sync)
    private final Long userId;
    private final Set<Role> roles;
    private final boolean active;

    /**
     * Checks if the user exists in the local database.
     */
    public boolean isRegistered() {
        return userId != null;
    }

    /**
     * Checks if the user has a specific role.
     */
    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Checks if the user has any of the specified roles.
     */
    public boolean hasAnyRole(Role... rolesToCheck) {
        if (roles == null) {
            return false;
        }
        for (Role role : rolesToCheck) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the user is a superadmin.
     */
    public boolean isSuperAdmin() {
        return hasRole(Role.SUPERADMIN);
    }
}
