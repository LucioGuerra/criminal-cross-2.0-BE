package org.athlium.auth.application.ports;

import org.athlium.auth.domain.model.User;
import java.util.Optional;

/**
 * Puerto (interface) para el repositorio de usuarios
 * Define el contrato sin implementación
 */
public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByFirebaseUid(String firebaseUid);
    User save(User user);
    void delete(String id);
}