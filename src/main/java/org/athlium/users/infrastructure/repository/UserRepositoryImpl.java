package org.athlium.users.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.athlium.users.infrastructure.entity.UserEntity;
import org.athlium.users.infrastructure.mapper.UserMapper;

import java.util.Optional;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {

    @Inject
    UserMapper userMapper;

    @Override
    public Optional<User> findByFirebaseUid(String firebaseUid) {
        return UserEntity.find("firebaseUid", firebaseUid)
                .firstResultOptional()
                .map(entity -> userMapper.toDomain((UserEntity) entity));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return UserEntity.find("email", email)
                .firstResultOptional()
                .map(entity -> userMapper.toDomain((UserEntity) entity));
    }

    @Override
    public User save(User user) {
        UserEntity entity;
        if (user.getId() != null) {
            entity = UserEntity.findById(user.getId());
            userMapper.updateEntity(user, entity);
        } else {
            entity = userMapper.toEntity(user);
        }
        entity.persist();
        return userMapper.toDomain(entity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return UserEntity.findByIdOptional(id)
                .map(entity -> userMapper.toDomain((UserEntity) entity));
    }

    @Override
    public void deleteById(Long id) {
        UserEntity.deleteById(id);
    }
}