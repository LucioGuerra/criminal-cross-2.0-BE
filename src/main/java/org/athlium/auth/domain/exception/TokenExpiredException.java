package org.athlium.auth.domain.exception;

/**
 * Exception thrown when a token has expired.
 */
public class TokenExpiredException extends AuthenticationException {

    public TokenExpiredException() {
        super("Token has expired");
    }

    public TokenExpiredException(String message) {
        super(message);
    }
}
