package org.athlium.users.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.users.domain.model.PackageStatus;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.domain.repository.UserQueryRepository;
import org.athlium.users.domain.repository.UserRepository;

import java.util.Optional;

@ApplicationScoped
public class GetUserByIdUseCase {

    @Inject
    UserQueryRepository userQueryRepository;

    @Inject
    UserRepository userRepository;

    public UserWithPackageStatus execute(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User ID must not be null");
        }

        UserWithPackageStatus result = userQueryRepository.findUserById(userId);
        if (result != null) {
            return result;
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User", userId);
        }

        User existingUser = user.get();
        UserWithPackageStatus noPackageUser = new UserWithPackageStatus();
        noPackageUser.setId(existingUser.getId());
        noPackageUser.setName(existingUser.getName());
        noPackageUser.setLastName(existingUser.getLastName());
        noPackageUser.setEmail(existingUser.getEmail());
        noPackageUser.setRoles(existingUser.getRoles());
        noPackageUser.setActive(existingUser.getActive());
        noPackageUser.setPackageStatus(PackageStatus.NO_PACKAGE);
        noPackageUser.setPeriodEnd(null);
        noPackageUser.setDaysRemaining(null);

        return noPackageUser;
    }
}
