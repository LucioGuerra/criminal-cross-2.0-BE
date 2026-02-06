package org.athlium.users.application.usecase;

import org.athlium.shared.exception.DomainException;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateUserUseCaseUnitTest {

    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateUserUseCase();
        useCase.userRepository = new InMemoryUserRepository();
    }

    @Test
    void shouldRejectDuplicateEmail() {
        useCase.execute("uid-1", "already@used.com", "Jane", "Doe");

        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute("uid-2", "already@used.com", "John", "Doe"));

        assertEquals("Email is already in use", ex.getMessage());
    }

    @Test
    void shouldReturnExistingWhenFirebaseUidAlreadyExists() {
        User existing = useCase.execute("uid-1", "first@used.com", "Jane", "Doe");

        User result = useCase.execute("uid-1", "another@mail.com", "John", "Doe");

        assertSame(existing, result);
        assertTrue(result.hasRole(Role.CLIENT));
    }

    private static class InMemoryUserRepository implements UserRepository {
        private final Map<Long, User> usersById = new HashMap<>();
        private long nextId = 100;

        @Override
        public Optional<User> findByFirebaseUid(String firebaseUid) {
            return usersById.values().stream()
                    .filter(user -> firebaseUid.equals(getField(user, "firebaseUid")))
                    .findFirst();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return usersById.values().stream()
                    .filter(user -> email.equals(getField(user, "email")))
                    .findFirst();
        }

        @Override
        public User save(User user) {
            if (getField(user, "id") == null) {
                setField(user, "id", nextId++);
            }
            usersById.put((Long) getField(user, "id"), user);
            return user;
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(usersById.get(id));
        }

        @Override
        public void deleteById(Long id) {
            usersById.remove(id);
        }

        private static Object getField(Object target, String fieldName) {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed reading field " + fieldName, e);
            }
        }

        private static void setField(Object target, String fieldName, Object value) {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed setting field " + fieldName, e);
            }
        }
    }
}
