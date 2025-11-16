package org.athlium.users.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;

@ApplicationScoped
public class SyncUserWithFirebaseUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    CreateUserUseCase createUserUseCase;

    public User execute(String firebaseUid, String email, String name, String lastName) {
        var existingUser = userRepository.findByFirebaseUid(firebaseUid);
        
        if (existingUser.isPresent()) {
            var user = existingUser.get();
            user.update(name, lastName);
            return userRepository.save(user);
        }

        return createUserUseCase.execute(firebaseUid, email, name, lastName);
    }
}