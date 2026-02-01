package org.athlium.users.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.shared.exception.DomainException;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;

import java.util.Set;

@ApplicationScoped
public class UpdateUserRolesUseCase {

    @Inject
    UserRepository userRepository;

    public User execute(String firebaseUid, Set<Role> newRoles, User currentUser) {
        if (!currentUser.hasRole(Role.SUPERADMIN) && !currentUser.hasRole(Role.ORG_ADMIN)) {
            throw new DomainException("Only ADMIN or SUPERADMIN can update user roles");
        }

        var user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new DomainException("User not found"));

        var updatedUser = User.builder()
                .id(user.getId())
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .roles(newRoles)
                .active(user.getActive())
                .build();

        return userRepository.save(updatedUser);
    }
}