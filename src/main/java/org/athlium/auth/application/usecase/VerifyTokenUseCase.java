package org.athlium.auth.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.DecodedToken;

/**
 * Use case for verifying a Firebase ID token and returning authenticated user data.
 */
@ApplicationScoped
public class VerifyTokenUseCase {

    @Inject
    TokenValidator tokenValidator;

    @Inject
    UserProvider userProvider;

    /**
     * Verifies a Firebase ID token and returns the authenticated user.
     *
     * @param idToken The Firebase ID token
     * @return AuthenticatedUser with enriched data from local database
     */
    public AuthenticatedUser execute(String idToken) {
        // Validate the token
        DecodedToken decodedToken = tokenValidator.validateToken(idToken);

        // Build authenticated user
        AuthenticatedUser.AuthenticatedUserBuilder builder = AuthenticatedUser.builder()
                .firebaseUid(decodedToken.getUid())
                .email(decodedToken.getEmail())
                .name(decodedToken.getName())
                .emailVerified(decodedToken.isEmailVerified())
                .provider(decodedToken.getProvider());

        // Enrich with local user data
        return userProvider
                .enrichWithUserData(decodedToken.getUid(), builder)
                .build();
    }
}
