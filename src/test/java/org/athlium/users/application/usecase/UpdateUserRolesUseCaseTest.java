package org.athlium.users.application.usecase;

import org.athlium.shared.exception.DomainException;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateUserRolesUseCaseTest {

    private UpdateUserRolesUseCase updateUserRolesUseCase;
    private CreateUserUseCase createUserUseCase;
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();

        createUserUseCase = new CreateUserUseCase();
        createUserUseCase.userRepository = userRepository;

        updateUserRolesUseCase = new UpdateUserRolesUseCase();
        updateUserRolesUseCase.userRepository = userRepository;
    }

    @Test
    void shouldUpdateRolesWhenCurrentUserIsOrgAdmin() {
        User targetUser = createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.CLIENT, Role.ORG_ADMIN));

        User updated = updateUserRolesUseCase.execute("target-uid", Set.of(Role.CLIENT, Role.PROFESSOR), currentUser);

        assertTrue(updated.hasRole(Role.CLIENT));
        assertTrue(updated.hasRole(Role.PROFESSOR));
        assertTrue(!targetUser.hasRole(Role.PROFESSOR));
    }

    @Test
    void shouldFailWhenCurrentUserIsNotAdmin() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("client-uid", "client@mail.com", "Client", "User");

        DomainException ex = assertThrows(DomainException.class,
                () -> updateUserRolesUseCase.execute("target-uid", Set.of(Role.PROFESSOR), currentUser));

        assertEquals("Only ADMIN or SUPERADMIN can update user roles", ex.getMessage());
    }

    @Test
    void shouldFailWhenRolesAreEmpty() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.CLIENT, Role.ORG_ADMIN));

        DomainException ex = assertThrows(DomainException.class,
                () -> updateUserRolesUseCase.execute("target-uid", Set.of(), currentUser));

        assertEquals("At least one role is required", ex.getMessage());
    }

    @Test
    void shouldFailWhenOrgAdminAssignsSuperAdminRole() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.CLIENT, Role.ORG_ADMIN));

        DomainException ex = assertThrows(DomainException.class,
                () -> updateUserRolesUseCase.execute("target-uid", Set.of(Role.SUPERADMIN), currentUser));

        assertEquals("Only SUPERADMIN can assign SUPERADMIN role", ex.getMessage());
    }

    @Test
    void shouldFailWhenOrgAdminTriesToUpdateSuperAdminUser() {
        User targetUser = createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        setRoles(targetUser, Set.of(Role.CLIENT, Role.SUPERADMIN));
        userRepository.save(targetUser);

        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.CLIENT, Role.ORG_ADMIN));

        DomainException ex = assertThrows(DomainException.class,
                () -> updateUserRolesUseCase.execute("target-uid", Set.of(Role.ORG_OWNER), currentUser));

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
