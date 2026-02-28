package org.athlium.users.infrastructure.repository;

import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.infrastructure.entity.UserEntity;
import org.athlium.users.infrastructure.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRepositoryImplTest {

    @Test
    void shouldUpdateManagedEntityWithoutCallingPersist() {
        var repository = new UserRepositoryImpl();
        var panache = new FakeUserPanacheRepository();
        var mapper = new FakeUserMapper();
        repository.userPanacheRepository = panache;
        repository.userMapper = mapper;

        var managed = new UserEntity();
        managed.id = 7L;
        managed.setName("Nombre viejo");
        managed.setLastName("Apellido viejo");
        managed.setEmail("old@example.com");
        managed.setFirebaseUid("uid-old");
        managed.setRoles(Set.of(Role.CLIENT));
        managed.setActive(true);
        panache.stored = managed;

        var update = User.builder()
                .id(7L)
                .name("Nombre nuevo")
                .lastName("Apellido nuevo")
                .email("new@example.com")
                .firebaseUid("uid-new")
                .roles(Set.of(Role.ORG_OWNER))
                .active(false)
                .build();

        repository.save(update);

        assertEquals(0, panache.persistCalls);
        assertEquals("Nombre nuevo", managed.getName());
        assertEquals("Apellido nuevo", managed.getLastName());
        assertEquals("new@example.com", managed.getEmail());
        assertEquals("uid-new", managed.getFirebaseUid());
        assertEquals(Set.of(Role.ORG_OWNER), managed.getRoles());
        assertEquals(false, managed.getActive());
    }

    private static final class FakeUserPanacheRepository extends UserPanacheRepository {
        UserEntity stored;
        int persistCalls;

        @Override
        public UserEntity findById(Long id) {
            return stored != null && stored.id.equals(id) ? stored : null;
        }

        @Override
        public void persist(UserEntity entity) {
            persistCalls++;
            stored = entity;
        }
    }

    private static final class FakeUserMapper implements UserMapper {

        @Override
        public User toDomain(UserEntity entity) {
            return User.builder()
                    .id(entity.id)
                    .name(entity.getName())
                    .lastName(entity.getLastName())
                    .email(entity.getEmail())
                    .firebaseUid(entity.getFirebaseUid())
                    .roles(entity.getRoles())
                    .active(entity.getActive())
                    .build();
        }

        @Override
        public UserEntity toEntity(User user) {
            var entity = new UserEntity();
            entity.id = user.getId();
            entity.setName(user.getName());
            entity.setLastName(user.getLastName());
            entity.setEmail(user.getEmail());
            entity.setFirebaseUid(user.getFirebaseUid());
            entity.setRoles(user.getRoles());
            entity.setActive(user.getActive());
            return entity;
        }

        @Override
        public void updateEntity(User user, UserEntity entity) {
            entity.setName(user.getName());
            entity.setLastName(user.getLastName());
            entity.setEmail(user.getEmail());
            entity.setFirebaseUid(user.getFirebaseUid());
            entity.setRoles(user.getRoles());
            entity.setActive(user.getActive());
        }
    }
}
