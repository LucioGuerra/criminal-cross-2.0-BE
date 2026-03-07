package org.athlium.auth.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.FirebaseIdentityProvider;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.exception.UserAlreadyExistsException;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.DecodedToken;
import org.athlium.auth.domain.model.FirebaseSessionTokens;
import org.athlium.users.domain.model.User;
import org.jboss.logging.Logger;

import java.util.Optional;

/**
 * Use case for registering a new user after Firebase authentication.
 * This creates the user in the local database using the Firebase token data.
 */
@ApplicationScoped
public class RegisterUserUseCase {

    private static final Logger LOG = Logger.getLogger(RegisterUserUseCase.class);

    @Inject
    FirebaseIdentityProvider firebaseIdentityProvider;

    @Inject
    TokenValidator tokenValidator;

    @Inject
    UserProvider userProvider;

    /**
     * Registers a new user in the local database.
     *
     * @param idToken  The Firebase ID token
     * @param name     User's first name
     * @param lastName User's last name
     * @return The authenticated user with registration complete
     * @throws AuthenticationException   if token validation fails
     * @throws UserAlreadyExistsException if user is already registered
     */
    public RegisterResult execute(String email, String password, String name, String lastName) {
        LOG.debugf("Registering user through Firebase BFF flow");

        String displayName = composeDisplayName(name, lastName);
        FirebaseSessionTokens firebaseTokens = firebaseIdentityProvider.register(email, password, displayName);

        DecodedToken decodedToken = tokenValidator.validateToken(firebaseTokens.accessToken());

        Optional<User> existingUser = userProvider.findByFirebaseUid(decodedToken.getUid());
        if (existingUser.isPresent()) {
            LOG.infof("User already registered: %s", decodedToken.getEmail());
            throw new UserAlreadyExistsException("User is already registered");
        }

        String userName = (name != null && !name.isBlank()) ? name : decodedToken.getName();
        String userLastName = (lastName != null && !lastName.isBlank()) ? lastName : "";

        User createdUser = userProvider.createUser(
                decodedToken.getUid(),
                decodedToken.getEmail(),
                userName,
                userLastName
        );

        LOG.infof("User registered successfully: %s (ID: %s)", createdUser.getEmail(), createdUser.getId());

        AuthenticatedUser user = AuthenticatedUser.builder()
                .firebaseUid(createdUser.getFirebaseUid())
                .email(createdUser.getEmail())
                .name(createdUser.getName())
                .emailVerified(decodedToken.isEmailVerified())
                .provider(decodedToken.getProvider())
                .userId(createdUser.getId())
                .roles(createdUser.getRoles())
                .active(createdUser.getActive())
                .build();

        return new RegisterResult(user, firebaseTokens);
    }

    private String composeDisplayName(String name, String lastName) {
        String safeName = name == null ? "" : name.trim();
        String safeLastName = lastName == null ? "" : lastName.trim();
        String displayName = (safeName + " " + safeLastName).trim();
        return displayName.isBlank() ? null : displayName;
    }

    public record RegisterResult(AuthenticatedUser user, FirebaseSessionTokens tokens) {}
}
