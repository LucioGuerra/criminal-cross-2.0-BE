package org.athlium.users.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;

import java.util.Set;

@ApplicationScoped
public class CreateUserUseCase {

    @Inject
    UserRepository userRepository;

    public User execute(String firebaseUid, String email, String name, String lastName) {
        var existingUser = userRepository.findByFirebaseUid(firebaseUid);
        if (existingUser.isPresent()) {
            return existingUser.get();
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