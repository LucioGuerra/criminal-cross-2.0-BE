package org.athlium.auth.application.service;

import org.athlium.auth.application.ports.UserRepository;
import org.athlium.auth.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

/**
 * Servicio de aplicación para casos de uso de autenticación
 */
@ApplicationScoped
public class AuthService {

//    @Inject
//    UserRepository userRepository;
//
//    public Optional<User> authenticateUser(String firebaseUid) {
//        return userRepository.findByFirebaseUid(firebaseUid);
//    }
//
//    public User registerUser(String email, String firebaseUid) {
//        User user = new User(null, email, firebaseUid);
//        return userRepository.save(user);
//    }
}