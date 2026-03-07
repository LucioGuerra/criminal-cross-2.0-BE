package org.athlium.auth.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.FirebaseIdentityProvider;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.DecodedToken;
import org.athlium.auth.domain.model.FirebaseSessionTokens;
import org.jboss.logging.Logger;

/**
 * Use case for Firebase-backed refresh token exchange.
 */
@ApplicationScoped
public class RefreshTokenUseCase {

    private static final Logger LOG = Logger.getLogger(RefreshTokenUseCase.class);

    @Inject
    FirebaseIdentityProvider firebaseIdentityProvider;

    @Inject
    TokenValidator tokenValidator;

    @Inject
    UserProvider userProvider;

    /**
     * Result of a successful token refresh.
     */
    public record RefreshResult(
            AuthenticatedUser user,
            FirebaseSessionTokens tokens
    ) {}

    /**
     * Refreshes tokens through Firebase.
     *
     * @param refreshTokenValue The refresh token string
     * @return Refresh result with user info and new Firebase tokens
     */
    public RefreshResult execute(String refreshTokenValue) {
        LOG.debugf("Processing Firebase token refresh request");

        FirebaseSessionTokens refreshedTokens = firebaseIdentityProvider.refresh(refreshTokenValue);
        DecodedToken decodedToken = tokenValidator.validateToken(refreshedTokens.accessToken());

        AuthenticatedUser authenticatedUser = userProvider.enrichWithUserData(
                decodedToken.getUid(),
                AuthenticatedUser.builder()
                        .firebaseUid(decodedToken.getUid())
                        .email(decodedToken.getEmail())
                        .name(decodedToken.getName())
                        .emailVerified(decodedToken.isEmailVerified())
                        .provider(decodedToken.getProvider())
        ).build();

        LOG.infof("Firebase token refreshed for uid %s", decodedToken.getUid());

        return new RefreshResult(authenticatedUser, refreshedTokens);
    }
}
