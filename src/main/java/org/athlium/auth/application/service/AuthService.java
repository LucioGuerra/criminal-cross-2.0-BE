package org.athlium.auth.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.application.usecase.*;
import org.athlium.auth.domain.model.AuthenticatedUser;

/**
 * Application service orchestrating authentication use cases.
 * This is a facade that delegates to specific use cases.
 */
@ApplicationScoped
public class AuthService {

    @Inject
    VerifyTokenUseCase verifyTokenUseCase;

    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;

    @Inject
    RegisterUserUseCase registerUserUseCase;

    @Inject
    LoginUseCase loginUseCase;

    @Inject
    RefreshTokenUseCase refreshTokenUseCase;

    @Inject
    LogoutUseCase logoutUseCase;

    @Inject
    TokenValidator tokenValidator;

    @Inject
    UserProvider userProvider;

    /**
     * Verifies a Firebase ID token and returns the authenticated user.
     *
     * @param idToken The Firebase ID token to verify
     * @return The authenticated user with local data
     */
    public AuthenticatedUser verifyToken(String idToken) {
        return verifyTokenUseCase.execute(idToken);
    }

    /**
     * Gets the current authenticated user from the security context.
     *
     * @return The current authenticated user
     */
    public AuthenticatedUser getCurrentUser() {
        return getCurrentUserUseCase.execute();
    }

    /**
     * Checks if the auth system is properly initialized.
     *
     * @return true if Firebase is ready
     */
    public boolean isReady() {
        return tokenValidator.isReady();
    }

    /**
     * Registers a new user in the local database.
     *
     * @param idToken  The Firebase ID token
     * @param name     User's first name
     * @param lastName User's last name
     * @return The authenticated user with registration complete
     */
    public AuthenticatedUser registerUser(String idToken, String name, String lastName) {
        return registerUserUseCase.execute(idToken, name, lastName);
    }

    /**
     * Performs login with a Firebase ID token.
     *
     * @param idToken    The Firebase ID token
     * @param deviceInfo Optional device information
     * @param ipAddress  Optional IP address
     * @return Login result with user and refresh token
     */
    public LoginUseCase.LoginResult login(String idToken, String deviceInfo, String ipAddress) {
        return loginUseCase.execute(idToken, deviceInfo, ipAddress);
    }

    /**
     * Refreshes tokens using a valid refresh token.
     *
     * @param refreshToken The refresh token
     * @param deviceInfo   Optional device information
     * @param ipAddress    Optional IP address
     * @return Refresh result with new tokens
     */
    public RefreshTokenUseCase.RefreshResult refreshToken(String refreshToken, String deviceInfo, String ipAddress) {
        return refreshTokenUseCase.execute(refreshToken, deviceInfo, ipAddress);
    }

    /**
     * Logs out by revoking a specific refresh token.
     *
     * @param refreshToken The refresh token to revoke
     * @return Logout result
     */
    public LogoutUseCase.LogoutResult logout(String refreshToken) {
        return logoutUseCase.execute(refreshToken);
    }

    /**
     * Logs out from all devices.
     *
     * @return Logout result
     */
    public LogoutUseCase.LogoutResult logoutAll() {
        return logoutUseCase.executeLogoutAll();
    }
}