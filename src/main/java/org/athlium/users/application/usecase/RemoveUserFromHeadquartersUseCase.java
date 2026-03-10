package org.athlium.users.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.shared.exception.DomainException;
import org.athlium.users.application.service.HeadquartersMembershipAuthorizationService;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;

import java.util.HashSet;

@ApplicationScoped
public class RemoveUserFromHeadquartersUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    HeadquartersMembershipAuthorizationService authorizationService;

    public User execute(String firebaseUid, Long headquartersId, User currentUser) {
        authorizationService.authorizeMembershipUpdate(firebaseUid, headquartersId, currentUser);

        var user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new DomainException("User not found"));

        if (user.hasRole(Role.SUPERADMIN) && !currentUser.hasRole(Role.SUPERADMIN)) {
            throw new DomainException("Only SUPERADMIN can update a SUPERADMIN user");
        }

        var headquartersIds = new HashSet<Long>();
        if (user.getHeadquartersIds() != null) {
            headquartersIds.addAll(user.getHeadquartersIds());
        }

        if (!headquartersIds.remove(headquartersId)) {
            throw new DomainException("User is not assigned to headquarters");
        }

        var updatedUser = User.builder()
                .id(user.getId())
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .headquartersIds(headquartersIds)
                .active(user.getActive())
                .build();

        return userRepository.save(updatedUser);
    }
}
