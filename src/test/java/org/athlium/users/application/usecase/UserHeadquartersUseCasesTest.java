package org.athlium.users.application.usecase;

import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.repository.HeadquartersRepository;
import org.athlium.shared.exception.DomainException;
import org.athlium.users.application.service.HeadquartersMembershipAuthorizationService;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserHeadquartersUseCasesTest {

    private AssignUserToHeadquartersUseCase assignUseCase;
    private RemoveUserFromHeadquartersUseCase removeUseCase;
    private CreateUserUseCase createUserUseCase;
    private InMemoryUserRepository userRepository;
    private StubHeadquartersRepository headquartersRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();

        createUserUseCase = new CreateUserUseCase();
        createUserUseCase.userRepository = userRepository;

        headquartersRepository = new StubHeadquartersRepository(Map.of(
                1L, 10L,
                2L, 10L,
                3L, 20L
        ));

        var authorizationService = new HeadquartersMembershipAuthorizationService();
        setField(authorizationService, "headquartersRepository", headquartersRepository);

        assignUseCase = new AssignUserToHeadquartersUseCase();
        assignUseCase.userRepository = userRepository;
        assignUseCase.authorizationService = authorizationService;

        removeUseCase = new RemoveUserFromHeadquartersUseCase();
        removeUseCase.userRepository = userRepository;
        removeUseCase.authorizationService = authorizationService;
    }

    @Test
    void shouldAllowSelfAssignForRegularClient() {
        User currentUser = createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        setRoles(currentUser, Set.of(Role.CLIENT));

        User updated = assignUseCase.execute("target-uid", 1L, currentUser);

        assertTrue(updated.getHeadquartersIds().contains(1L));
    }

    @Test
    void shouldFailWhenAssigningToUnknownHeadquarters() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.ORG_ADMIN));

        DomainException ex = assertThrows(DomainException.class,
                () -> assignUseCase.execute("target-uid", 999L, currentUser));

        assertEquals("Headquarters not found", ex.getMessage());
    }

    @Test
    void shouldFailWhenAssigningAnAlreadyAssignedHeadquarters() {
        User target = createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        setHeadquarters(target, Set.of(1L));
        userRepository.save(target);
        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.ORG_ADMIN));
        setHeadquarters(currentUser, Set.of(2L));
        userRepository.save(currentUser);

        DomainException ex = assertThrows(DomainException.class,
                () -> assignUseCase.execute("target-uid", 1L, currentUser));

        assertEquals("User is already assigned to headquarters", ex.getMessage());
    }

    @Test
    void shouldFailWhenAssigningAnotherUserWithoutAdminOrOwnerRole() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("client-uid", "client@mail.com", "Client", "User");

        DomainException ex = assertThrows(DomainException.class,
                () -> assignUseCase.execute("target-uid", 1L, currentUser));

        assertEquals("Only headquarters admins, owners, or SUPERADMIN can update other users", ex.getMessage());
    }

    @Test
    void shouldAllowOrgAdminToAssignAnotherUserInSameOrganization() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.ORG_ADMIN));
        setHeadquarters(currentUser, Set.of(2L));
        userRepository.save(currentUser);

        User updated = assignUseCase.execute("target-uid", 1L, currentUser);

        assertTrue(updated.getHeadquartersIds().contains(1L));
    }

    @Test
    void shouldAllowOrgOwnerToRemoveAnotherUserInSameOrganization() {
        User target = createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        setHeadquarters(target, Set.of(1L, 2L));
        userRepository.save(target);
        User currentUser = createUserUseCase.execute("owner-uid", "owner@mail.com", "Owner", "User");
        setRoles(currentUser, Set.of(Role.ORG_OWNER));
        setHeadquarters(currentUser, Set.of(2L));
        userRepository.save(currentUser);

        User updated = removeUseCase.execute("target-uid", 1L, currentUser);

        assertEquals(Set.of(2L), updated.getHeadquartersIds());
    }

    @Test
    void shouldAllowSelfRemoveForRegularClient() {
        User currentUser = createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        setRoles(currentUser, Set.of(Role.CLIENT));
        setHeadquarters(currentUser, Set.of(1L, 2L));
        userRepository.save(currentUser);

        User updated = removeUseCase.execute("target-uid", 1L, currentUser);

        assertEquals(Set.of(2L), updated.getHeadquartersIds());
    }

    @Test
    void shouldFailWhenRemovingUnassignedHeadquarters() {
        createUserUseCase.execute("target-uid", "target@mail.com", "Target", "User");
        User currentUser = createUserUseCase.execute("admin-uid", "admin@mail.com", "Admin", "User");
        setRoles(currentUser, Set.of(Role.ORG_ADMIN));
        setHeadquarters(currentUser, Set.of(2L));
        userRepository.save(currentUser);

        DomainException ex = assertThrows(DomainException.class,
                () -> removeUseCase.execute("target-uid", 1L, currentUser));

        assertEquals("User is not assigned to headquarters", ex.getMessage());
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

    private static class StubHeadquartersRepository implements HeadquartersRepository {
        private final Map<Long, Long> organizationByHeadquartersId;

        private StubHeadquartersRepository(Map<Long, Long> organizationByHeadquartersId) {
            this.organizationByHeadquartersId = organizationByHeadquartersId;
        }

        @Override
        public Headquarters save(Headquarters headquarters) {
            return headquarters;
        }

        @Override
        public Optional<Headquarters> findById(Long id) {
            if (!organizationByHeadquartersId.containsKey(id)) {
                return Optional.empty();
            }
            return Optional.of(Headquarters.builder()
                    .id(id)
                    .organizationId(organizationByHeadquartersId.get(id))
                    .build());
        }

        @Override
        public List<Headquarters> findAll() {
            return List.of();
        }

        @Override
        public List<Headquarters> findByOrganizationId(Long organizationId) {
            return List.of();
        }

        @Override
        public void deleteById(Long id) {
        }

        @Override
        public boolean existsById(Long id) {
            return organizationByHeadquartersId.containsKey(id);
        }
    }

    private static void setRoles(User user, Set<Role> roles) {
        setField(user, "roles", new HashSet<>(roles));
    }

    private static void setHeadquarters(User user, Set<Long> headquartersIds) {
        setField(user, "headquartersIds", new HashSet<>(headquartersIds));
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
