package org.athlium.auth.application.ports;

import org.athlium.auth.domain.model.DecodedToken;

/**
 * Port interface for token validation.
 * Implementations handle the actual validation logic (Firebase, mock, etc.)
 */
public interface TokenValidator {

    /**
     * Validates a Firebase ID token and extracts claims.
     *
     * @param idToken The Firebase ID token to validate
     * @return DecodedToken with the extracted claims
     * @throws org.athlium.auth.domain.exception.InvalidTokenException if token is invalid
     * @throws org.athlium.auth.domain.exception.TokenExpiredException if token has expired
     */
    DecodedToken validateToken(String idToken);

    /**
     * Checks if the token validator is properly configured and ready.
     *
     * @return true if the validator is ready to validate tokens
     */
    boolean isReady();
}
