package org.athlium.users.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.shared.exception.DomainException;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;

@ApplicationScoped
public class UpdateUserUseCase {

    @Inject
    UserRepository userRepository;

    public User execute(Long userId, String email, String name, String lastName, Boolean active, User currentUser) {
        validateAdminPrivileges(currentUser);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found"));

        if (user.hasRole(Role.SUPERADMIN) && !currentUser.hasRole(Role.SUPERADMIN)) {
            throw new DomainException("Only SUPERADMIN can update a SUPERADMIN user");
        }

        validateUniqueEmail(email, user);

        var updatedUser = User.builder()
                .id(user.getId())
                .firebaseUid(user.getFirebaseUid())
                .email(email)
                .name(name)
                .lastName(lastName)
                .roles(user.getRoles())
                .headquartersIds(user.getHeadquartersIds())
                .active(active)
                .build();

        return userRepository.save(updatedUser);
    }

    private void validateAdminPrivileges(User currentUser) {
        if (!currentUser.hasRole(Role.SUPERADMIN) && !currentUser.hasRole(Role.ORG_ADMIN)) {
            throw new DomainException("Only ADMIN or SUPERADMIN can update users");
        }
    }

    private void validateUniqueEmail(String email, User current) {
        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(current.getId()))
                .ifPresent(existing -> {
                    throw new DomainException("Email is already in use");
                });
    }
}
