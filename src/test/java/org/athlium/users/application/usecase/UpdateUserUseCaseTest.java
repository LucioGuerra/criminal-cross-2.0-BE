package org.athlium.users.application.usecase;

import org.athlium.shared.exception.DomainException;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateUserUseCaseTest {

    private UpdateUserUseCase updateUserUseCase;
    private CreateUserUseCase createUserUseCase;
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();

        createUserUseCase = new CreateUserUseCase();
        createUserUseCase.userRepository = userRepository;

        updateUserUseCase = new UpdateUserUseCase();
        updateUserUseCase.userRepository = userRepository;
    }

    @Test
    void shouldUpdateEditableFieldsWhenCurrentUserIsOrgAdmin() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.CLIENT, Role.ORG_ADMIN));

        User updated = updateUserUseCase.execute("target-uid", "target-updated@mail.com", "Target Updated", "User Updated", false,
                currentUser);

        assertEquals("target-updated@mail.com", updated.getEmail());
        assertEquals("Target Updated", updated.getName());
        assertEquals("User Updated", updated.getLastName());
        assertEquals(false, updated.getActive());
    }

    @Test
    void shouldFailWhenCurrentUserIsNotAdmin() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("client-uid", "client@mail.com", "Client", "User");

        DomainException ex = assertThrows(DomainException.class,
                () -> updateUserUseCase.execute("target-uid", "other@mail.com", "Other", "User", true, currentUser));

        assertEquals("Only ADMIN or SUPERADMIN can update users", ex.getMessage());
    }

    @Test
    void shouldFailWhenEmailAlreadyExistsForDifferentUser() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        createUserUseCase.execute("other-uid", "other@mail.com", "Other", "User");
        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.CLIENT, Role.ORG_ADMIN));

        DomainException ex = assertThrows(DomainException.class,
                () -> updateUserUseCase.execute("target-uid", "other@mail.com", "Target", "User", true, currentUser));

        assertEquals("Email is already in use", ex.getMessage());
    }

    @Test
    void shouldFailWhenOrgAdminUpdatesSuperAdminUser() {
        User targetUser = createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        setRoles(targetUser, Set.of(Role.CLIENT, Role.SUPERADMIN));
        userRepository.save(targetUser);

        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.CLIENT, Role.ORG_ADMIN));

        DomainException ex = assertThrows(DomainException.class,
                () -> updateUserUseCase.execute("target-uid", "target@mail.com", "Target", "User", true, currentUser));

        assertEquals("Only SUPERADMIN can update a SUPERADMIN user", ex.getMessage());
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

    private static void setRoles(User user, Set<Role> roles) {
        setField(user, "roles", new HashSet<>(roles));
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
