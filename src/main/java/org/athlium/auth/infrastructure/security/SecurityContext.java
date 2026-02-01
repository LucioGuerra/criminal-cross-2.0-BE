package org.athlium.auth.infrastructure.security;

import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;
import lombok.Setter;
import org.athlium.auth.domain.model.AuthenticatedUser;

/**
 * Request-scoped holder for the current authenticated user.
 * Populated by the AuthFilter and accessible throughout the request.
 */
@RequestScoped
@Getter
@Setter
public class SecurityContext {

    private AuthenticatedUser currentUser;
    private String rawToken;
    private boolean authenticated;

    /**
     * Checks if there is an authenticated user in the current request.
     */
    public boolean isAuthenticated() {
        return authenticated && currentUser != null;
    }

    /**
     * Gets the current user or throws if not authenticated.
     */
    public AuthenticatedUser requireCurrentUser() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in current request");
        }
        return currentUser;
    }

    /**
     * Gets the Firebase UID of the current user, or null if not authenticated.
     */
    public String getCurrentFirebaseUid() {
        return currentUser != null ? currentUser.getFirebaseUid() : null;
    }
}
