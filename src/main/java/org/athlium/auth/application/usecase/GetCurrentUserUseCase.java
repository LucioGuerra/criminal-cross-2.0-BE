package org.athlium.auth.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.domain.exception.UnauthorizedException;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.security.SecurityContext;

/**
 * Use case for getting the current authenticated user from the security context.
 */
@ApplicationScoped
public class GetCurrentUserUseCase {

    @Inject
    SecurityContext securityContext;

    /**
     * Gets the current authenticated user.
     *
     * @return The authenticated user
     * @throws UnauthorizedException if no user is authenticated
     */
    public AuthenticatedUser execute() {
        if (!securityContext.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user");
        }
        return securityContext.getCurrentUser();
    }

    /**
     * Gets the current user if authenticated, or null otherwise.
     *
     * @return The authenticated user or null
     */
    public AuthenticatedUser executeOptional() {
        return securityContext.isAuthenticated() 
                ? securityContext.getCurrentUser() 
                : null;
    }
}
