package org.athlium.users.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;

import java.util.Optional;

@ApplicationScoped
public class GetUserByUidUseCase {

    @Inject
    UserRepository userRepository;

    public Optional<User> execute(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }
}