package org.athlium.auth.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.exception.UserAlreadyExistsException;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.DecodedToken;
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
    public AuthenticatedUser execute(String idToken, String name, String lastName) {
        LOG.debugf("Registering user with token");

        // Validate the token first
        DecodedToken decodedToken = tokenValidator.validateToken(idToken);

        // Check if user already exists
        Optional<User> existingUser = userProvider.findByFirebaseUid(decodedToken.getUid());
        if (existingUser.isPresent()) {
            LOG.infof("User already registered: %s", decodedToken.getEmail());
            throw new UserAlreadyExistsException("User is already registered");
        }

        // Use name from request, fallback to token data if not provided
        String userName = (name != null && !name.isBlank()) ? name : decodedToken.getName();
        String userLastName = (lastName != null && !lastName.isBlank()) ? lastName : "";

        // Create the user in the database
        User createdUser = userProvider.createUser(
                decodedToken.getUid(),
                decodedToken.getEmail(),
                userName,
                userLastName
        );

        LOG.infof("User registered successfully: %s (ID: %s)", createdUser.getEmail(), createdUser.getId());

        // Build and return the authenticated user
        return AuthenticatedUser.builder()
                .firebaseUid(createdUser.getFirebaseUid())
                .email(createdUser.getEmail())
                .name(createdUser.getName())
                .emailVerified(decodedToken.isEmailVerified())
                .provider(decodedToken.getProvider())
                .userId(createdUser.getId())
                .roles(createdUser.getRoles())
                .active(createdUser.getActive())
                .build();
    }
}
