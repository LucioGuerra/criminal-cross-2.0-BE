package org.athlium.users.domain.repository;

import org.athlium.users.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    
    Optional<User> findByFirebaseUid(String firebaseUid);
    
    Optional<User> findByEmail(String email);
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    void deleteById(Long id);
}