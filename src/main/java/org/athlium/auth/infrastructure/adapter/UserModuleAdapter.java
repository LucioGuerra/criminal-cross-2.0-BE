package org.athlium.auth.infrastructure.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.users.application.usecase.CreateUserUseCase;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Adapter that connects the Auth module with the Users module.
 * Implements the UserProvider port defined in the auth application layer.
 */
@ApplicationScoped
public class UserModuleAdapter implements UserProvider {

    private static final Logger LOG = Logger.getLogger(UserModuleAdapter.class);

    @Inject
    UserRepository userRepository;

    @Inject
    CreateUserUseCase createUserUseCase;

    @Override
    public Optional<User> findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User syncUser(String firebaseUid, String email, String name) {
        // First try to find by Firebase UID
        Optional<User> existingByUid = userRepository.findByFirebaseUid(firebaseUid);
        if (existingByUid.isPresent()) {
            LOG.debugf("User found by Firebase UID: %s", firebaseUid);
            return existingByUid.get();
        }

        // Then try to find by email (for account linking scenarios)
        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            LOG.infof("User found by email, updating Firebase UID: %s -> %s", email, firebaseUid);
            // In a real scenario, you might want to update the firebaseUid
            // For now, just return the existing user
            return existingByEmail.get();
        }

        // User doesn't exist - return null, let the caller handle registration
        LOG.debugf("No existing user found for Firebase UID: %s or email: %s", firebaseUid, email);
        return null;
    }

    @Override
    public AuthenticatedUser.AuthenticatedUserBuilder enrichWithUserData(
            String firebaseUid,
            AuthenticatedUser.AuthenticatedUserBuilder builder) {

        Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            LOG.debugf("Enriching auth user with local data: userId=%d, roles=%s", 
                    user.getId(), user.getRoles());

            return builder
                    .userId(user.getId())
                    .roles(user.getRoles() != null ? user.getRoles() : new HashSet<>())
                    .active(user.getActive() != null && user.getActive());
        } else {
            LOG.debugf("No local user found for Firebase UID: %s", firebaseUid);
            // Return builder without local user data
            return builder
                    .userId(null)
                    .roles(Set.of())
                    .active(false);
        }
    }

    @Override
    public User createUser(String firebaseUid, String email, String name, String lastName) {
        LOG.infof("Creating new user: email=%s, firebaseUid=%s", email, firebaseUid);
        return createUserUseCase.execute(firebaseUid, email, name, lastName);
    }
}
