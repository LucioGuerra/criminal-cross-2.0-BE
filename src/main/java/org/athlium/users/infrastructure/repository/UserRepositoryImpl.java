package org.athlium.users.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.athlium.users.infrastructure.mapper.UserMapper;

import java.util.Optional;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {

    @Inject
    UserMapper userMapper;

    @Inject
    UserPanacheRepository userPanacheRepository;

    @Override
    public Optional<User> findByFirebaseUid(String firebaseUid) {
        return userPanacheRepository.find("firebaseUid", firebaseUid)
                .firstResultOptional()
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userPanacheRepository.find("email", email)
                .firstResultOptional()
                .map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        var entity = user.getId() != null ? userPanacheRepository.findById(user.getId()) : null;
        if (user.getId() != null) {
            userMapper.updateEntity(user, entity);
        } else {
            entity = userMapper.toEntity(user);
            userPanacheRepository.persist(entity);
        }
        return userMapper.toDomain(entity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userPanacheRepository.findByIdOptional(id)
                .map(userMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        userPanacheRepository.deleteById(id);
    }
}
