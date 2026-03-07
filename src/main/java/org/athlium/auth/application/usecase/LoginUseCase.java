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
 * Use case for Firebase-backed login.
 */
@ApplicationScoped
public class LoginUseCase {

    private static final Logger LOG = Logger.getLogger(LoginUseCase.class);

    @Inject
    FirebaseIdentityProvider firebaseIdentityProvider;

    @Inject
    TokenValidator tokenValidator;

    @Inject
    UserProvider userProvider;

    /**
     * Result of a successful login.
     */
    public record LoginResult(
            AuthenticatedUser user,
            FirebaseSessionTokens tokens
    ) {}

    /**
     * Performs login with Firebase credentials.
     *
     * @param email    User email
     * @param password User password
     * @return Login result with user info and Firebase tokens
     */
    public LoginResult execute(String email, String password) {
        LOG.debugf("Processing Firebase credential login for %s", email);

        FirebaseSessionTokens tokens = firebaseIdentityProvider.login(email, password);
        DecodedToken decodedToken = tokenValidator.validateToken(tokens.accessToken());

        AuthenticatedUser authenticatedUser = userProvider.enrichWithUserData(
                decodedToken.getUid(),
                AuthenticatedUser.builder()
                        .firebaseUid(decodedToken.getUid())
                        .email(decodedToken.getEmail())
                        .name(decodedToken.getName())
                        .emailVerified(decodedToken.isEmailVerified())
                        .provider(decodedToken.getProvider())
        ).build();

        LOG.infof("User logged in successfully: %s", decodedToken.getEmail());

        return new LoginResult(authenticatedUser, tokens);
    }
}
