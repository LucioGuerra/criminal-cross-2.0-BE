package org.athlium.users.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.shared.exception.DomainException;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;

import java.util.Set;

@ApplicationScoped
public class CreateUserUseCase {

    @Inject
    UserRepository userRepository;

    @Transactional
    public User execute(String firebaseUid, String email, String name, String lastName) {
        var existingUser = userRepository.findByFirebaseUid(firebaseUid);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new DomainException("Email is already in use");
        }

        var user = User.builder()
                .firebaseUid(firebaseUid)
                .email(email)
                .name(name)
                .lastName(lastName)
                .roles(Set.of(Role.CLIENT))
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
