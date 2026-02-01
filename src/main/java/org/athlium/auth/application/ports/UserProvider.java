package org.athlium.auth.application.ports;

import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.users.domain.model.User;

import java.util.Optional;

/**
 * Port interface for accessing user data from the Users module.
 * This acts as an anti-corruption layer between Auth and Users modules.
 */
public interface UserProvider {

    /**
     * Finds a user by their Firebase UID.
     *
     * @param firebaseUid The Firebase UID
     * @return Optional containing the user if found
     */
    Optional<User> findByFirebaseUid(String firebaseUid);

    /**
     * Finds a user by their email.
     *
     * @param email The user's email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Creates or updates a user during the sync process.
     * This is called when a user authenticates but doesn't exist locally.
     *
     * @param firebaseUid Firebase UID
     * @param email User's email
     * @param name User's display name (can be null)
     * @return The created or updated user
     */
    User syncUser(String firebaseUid, String email, String name);

    /**
     * Enriches a basic authenticated user with data from the local database.
     *
     * @param firebaseUid Firebase UID to look up
     * @param builder The AuthenticatedUser builder to enrich
     * @return The enriched builder
     */
    AuthenticatedUser.AuthenticatedUserBuilder enrichWithUserData(
            String firebaseUid, 
            AuthenticatedUser.AuthenticatedUserBuilder builder
    );

    /**
     * Creates a new user in the local database.
     *
     * @param firebaseUid Firebase UID
     * @param email       User's email
     * @param name        User's first name
     * @param lastName    User's last name
     * @return The created user
     */
    User createUser(String firebaseUid, String email, String name, String lastName);
}
