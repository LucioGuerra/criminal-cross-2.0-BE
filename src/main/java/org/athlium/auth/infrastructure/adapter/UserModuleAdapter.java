package org.athlium.auth.infrastructure.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.UserProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.repository.HeadquartersRepository;
import org.athlium.gym.domain.repository.OrganizationRepository;
import org.athlium.users.application.usecase.CreateUserUseCase;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    @Inject
    HeadquartersRepository headquartersRepository;

    @Inject
    OrganizationRepository organizationRepository;

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

        Optional<User> userOpt = findUserByFirebaseUid(firebaseUid);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            LOG.debugf("Enriching auth user with local data: userId=%d, roles=%s", 
                    user.getId(), user.getRoles());

            return builder
                    .userId(user.getId())
                    .roles(user.getRoles() != null ? user.getRoles() : new HashSet<>())
                    .organizationId(resolveOrganizationId(user).orElse(null))
                    .organizationName(resolveOrganizationName(user).orElse(null))
                    .headquarters(resolveHeadquarters(user))
                    .active(user.getActive() != null && user.getActive());
        } else {
            LOG.debugf("No local user found for Firebase UID: %s", firebaseUid);
            // Return builder without local user data
            return builder
                    .userId(null)
                    .roles(Set.of())
                    .organizationId(null)
                    .organizationName(null)
                    .headquarters(List.of())
                    .active(false);
        }
    }

    private Optional<Long> resolveOrganizationId(User user) {
        OrganizationSelection selection = selectOrganizationWithHeadquarters(user);
        if (selection == null) {
            return Optional.empty();
        }
        return Optional.of(selection.organizationId());
    }

    private Optional<String> resolveOrganizationName(User user) {
        OrganizationSelection selection = selectOrganizationWithHeadquarters(user);
        if (selection == null) {
            return Optional.empty();
        }

        return organizationRepository.findById(selection.organizationId())
                .map(org -> org.getName())
                .or(() -> Optional.ofNullable(selection.organizationName()));
    }

    private List<AuthenticatedUser.AuthenticatedHeadquarters> resolveHeadquarters(User user) {
        OrganizationSelection selection = selectOrganizationWithHeadquarters(user);
        if (selection == null) {
            return List.of();
        }

        return selection.headquarters().stream()
                .map(headquarter -> AuthenticatedUser.AuthenticatedHeadquarters.builder()
                        .id(headquarter.getId())
                        .name(headquarter.getName())
                        .build())
                .toList();
    }

    private OrganizationSelection selectOrganizationWithHeadquarters(User user) {
        if (user.getHeadquartersIds() == null || user.getHeadquartersIds().isEmpty()) {
            return null;
        }

        Map<Long, List<Headquarters>> headquartersByOrganization = new HashMap<>();
        for (Long headquartersId : user.getHeadquartersIds()) {
            headquartersRepository.findById(headquartersId)
                    .ifPresent(headquarters -> headquartersByOrganization
                            .computeIfAbsent(headquarters.getOrganizationId(), ignored -> new ArrayList<>())
                            .add(headquarters));
        }

        if (headquartersByOrganization.isEmpty()) {
            return null;
        }

        return headquartersByOrganization.entrySet().stream()
                .map(entry -> new OrganizationSelection(
                        entry.getKey(),
                        organizationRepository.findById(entry.getKey()).map(org -> org.getName()).orElse(null),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(Headquarters::getName))
                                .toList()))
                .max(Comparator
                        .comparingInt((OrganizationSelection selection) -> selection.headquarters().size())
                        .thenComparing(OrganizationSelection::organizationId, Comparator.reverseOrder()))
                .orElse(null);
    }

    private record OrganizationSelection(Long organizationId,
                                         String organizationName,
                                         List<Headquarters> headquarters) {
    }

    private Optional<User> findUserByFirebaseUid(String firebaseUid) {
        Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
        if (userOpt.isPresent()) {
            return userOpt;
        }

        if (firebaseUid != null && firebaseUid.startsWith("mock-") && firebaseUid.length() > 5) {
            String withoutMockPrefix = firebaseUid.substring(5);
            return userRepository.findByFirebaseUid(withoutMockPrefix);
        }

        return Optional.empty();
    }

    @Override
    public User createUser(String firebaseUid, String email, String name, String lastName) {
        LOG.infof("Creating new user: email=%s, firebaseUid=%s", email, firebaseUid);
        return createUserUseCase.execute(firebaseUid, email, name, lastName);
    }
}
