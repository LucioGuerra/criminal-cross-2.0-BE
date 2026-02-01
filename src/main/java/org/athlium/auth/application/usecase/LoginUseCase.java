package org.athlium.auth.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.RefreshTokenStore;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.exception.AuthenticationException;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.domain.model.DecodedToken;
import org.athlium.auth.domain.model.RefreshToken;
import org.athlium.auth.infrastructure.security.JwtTokenGenerator;
import org.athlium.users.domain.model.User;
import org.jboss.logging.Logger;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use case for user login.
 * Validates the Firebase token and generates a refresh token + custom JWT.
 */
@ApplicationScoped
public class LoginUseCase {

    private static final Logger LOG = Logger.getLogger(LoginUseCase.class);

    @Inject
    TokenValidator tokenValidator;

    @Inject
    UserProvider userProvider;

    @Inject
    RefreshTokenStore refreshTokenStore;

    @Inject
    JwtTokenGenerator jwtTokenGenerator;

    /**
     * Result of a successful login.
     */
    public record LoginResult(
            AuthenticatedUser user,
            RefreshToken refreshToken,
            String accessToken,
            boolean needsRegistration
    ) {}

    /**
     * Performs login with a Firebase ID token.
     *
     * @param idToken    The Firebase ID token
     * @param deviceInfo Optional device information for session tracking
     * @param ipAddress  Optional IP address
     * @return Login result with user info and refresh token
     * @throws AuthenticationException if token validation fails
     */
    public LoginResult execute(String idToken, String deviceInfo, String ipAddress) {
        LOG.debugf("Processing login request");

        // Validate the Firebase token
        DecodedToken decodedToken = tokenValidator.validateToken(idToken);

        // Check if user exists in local database
        Optional<User> userOpt = userProvider.findByFirebaseUid(decodedToken.getUid());

        if (userOpt.isEmpty()) {
            // User needs to register first
            LOG.infof("User not registered: %s", decodedToken.getEmail());
            
            AuthenticatedUser unregisteredUser = AuthenticatedUser.builder()
                    .firebaseUid(decodedToken.getUid())
                    .email(decodedToken.getEmail())
                    .name(decodedToken.getName())
                    .emailVerified(decodedToken.isEmailVerified())
                    .provider(decodedToken.getProvider())
                    .userId(null)
                    .roles(null)
                    .active(false)
                    .build();

            return new LoginResult(unregisteredUser, null, null, true);
        }

        User user = userOpt.get();

        // Check if user is active
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AuthenticationException("User account is deactivated");
        }

        // Build authenticated user
        AuthenticatedUser authenticatedUser = userProvider.enrichWithUserData(
                decodedToken.getUid(),
                AuthenticatedUser.builder()
                        .firebaseUid(decodedToken.getUid())
                        .email(decodedToken.getEmail())
                        .name(user.getName())
                        .emailVerified(decodedToken.isEmailVerified())
                        .provider(decodedToken.getProvider())
        ).build();

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenStore.createToken(
                decodedToken.getUid(),
                user.getId(),
                deviceInfo,
                ipAddress
        );

        // Generate custom JWT access token
        String accessToken = jwtTokenGenerator.generateAccessToken(
                user.getFirebaseUid(),
                user.getEmail(),
                user.getId(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );

        LOG.infof("User logged in successfully: %s (ID: %d)", user.getEmail(), user.getId());

        return new LoginResult(authenticatedUser, refreshToken, accessToken, false);
    }
}
