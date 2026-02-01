package org.athlium.auth.domain.exception;

/**
 * Exception thrown when a user is not authorized to perform an action.
 */
public class UnauthorizedException extends AuthenticationException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public static UnauthorizedException insufficientPermissions() {
        return new UnauthorizedException("Insufficient permissions to perform this action");
    }

    public static UnauthorizedException userNotActive() {
        return new UnauthorizedException("User account is not active");
    }

    public static UnauthorizedException userNotRegistered() {
        return new UnauthorizedException("User is not registered in the system");
    }
}
