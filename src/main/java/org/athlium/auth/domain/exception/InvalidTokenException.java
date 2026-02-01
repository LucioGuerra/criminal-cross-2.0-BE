package org.athlium.auth.domain.exception;

/**
 * Exception thrown when a token is invalid or malformed.
 */
public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidTokenException missingToken() {
        return new InvalidTokenException("Authorization token is missing");
    }

    public static InvalidTokenException malformed() {
        return new InvalidTokenException("Token is malformed or invalid");
    }

    public static InvalidTokenException invalidSignature() {
        return new InvalidTokenException("Token signature is invalid");
    }
}
