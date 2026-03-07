package org.athlium.auth.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.application.usecase.*;
import org.athlium.auth.domain.model.AuthenticatedUser;

/**
 * Application service orchestrating authentication use cases.
 * This is a facade that delegates to specific use cases.
 */
@ApplicationScoped
public class AuthService {

    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;

    @Inject
    RegisterUserUseCase registerUserUseCase;

    @Inject
    LoginUseCase loginUseCase;

    @Inject
    RefreshTokenUseCase refreshTokenUseCase;

    @Inject
    TokenValidator tokenValidator;

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
     * Registers a new user through the API/Firebase BFF flow.
     *
     * @param email    User email
     * @param password User password
     * @param name     User's first name
     * @param lastName User's last name
     * @return Registration result with user and Firebase tokens
     */
    public RegisterUserUseCase.RegisterResult registerUser(String email, String password, String name, String lastName) {
        return registerUserUseCase.execute(email, password, name, lastName);
    }

    /**
     * Performs login with Firebase credentials.
     *
     * @param email    User email
     * @param password User password
     * @return Login result with user and Firebase tokens
     */
    public LoginUseCase.LoginResult login(String email, String password) {
        return loginUseCase.execute(email, password);
    }

    /**
     * Refreshes tokens using a valid refresh token.
     *
     * @param refreshToken The refresh token
     * @return Refresh result with refreshed Firebase tokens
     */
    public RefreshTokenUseCase.RefreshResult refreshToken(String refreshToken) {
        return refreshTokenUseCase.execute(refreshToken);
    }

}
