package org.athlium.auth.infrastructure.adapter;

import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.domain.repository.HeadquartersRepository;
import org.athlium.gym.domain.repository.OrganizationRepository;
import org.athlium.users.application.usecase.CreateUserUseCase;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserModuleAdapterUnitTest {

    private UserModuleAdapter adapter;
    private StubUserRepository userRepository;
    private StubHeadquartersRepository headquartersRepository;
    private StubOrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        adapter = new UserModuleAdapter();
        userRepository = new StubUserRepository();
        headquartersRepository = new StubHeadquartersRepository();
        organizationRepository = new StubOrganizationRepository();

        adapter.userRepository = userRepository;
        adapter.createUserUseCase = new CreateUserUseCase();
        adapter.headquartersRepository = headquartersRepository;
        adapter.organizationRepository = organizationRepository;
    }

    @Test
    void shouldSelectOrganizationWithMostHeadquartersWhenUserBelongsToMultipleOrgs() {
        User orgAdmin = User.builder()
                .id(7L)
                .firebaseUid("org-admin")
                .email("admin@test.com")
                .name("Org")
                .lastName("Admin")
                .roles(Set.of(Role.ORG_ADMIN))
                .headquartersIds(Set.of(20L, 21L, 30L))
                .active(true)
                .build();

        userRepository.userByUid = orgAdmin;
        headquartersRepository.headquartersById.put(20L, Headquarters.builder().id(20L).organizationId(2L).name("HQ A").build());
        headquartersRepository.headquartersById.put(21L, Headquarters.builder().id(21L).organizationId(2L).name("HQ B").build());
        headquartersRepository.headquartersById.put(30L, Headquarters.builder().id(30L).organizationId(3L).name("HQ C").build());
        organizationRepository.organizationsById.put(2L, Organization.builder().id(2L).name("Org Two").build());
        organizationRepository.organizationsById.put(3L, Organization.builder().id(3L).name("Org Three").build());

        AuthenticatedUser authenticatedUser = adapter.enrichWithUserData(
                "org-admin",
                AuthenticatedUser.builder()
                        .firebaseUid("org-admin")
                        .email("admin@test.com")
                        .name("Org Admin")
                        .provider(AuthProvider.EMAIL)
                        .emailVerified(true)
        ).build();

        assertEquals(2L, authenticatedUser.getOrganizationId());
        assertEquals("Org Two", authenticatedUser.getOrganizationName());
        assertEquals(2, authenticatedUser.getHeadquarters().size());
        assertEquals(20L, authenticatedUser.getHeadquarters().get(0).getId());
        assertEquals("HQ A", authenticatedUser.getHeadquarters().get(0).getName());
    }

    @Test
    void shouldResolveMockPrefixedUidToPersistedUserUid() {
        User superadmin = User.builder()
                .id(1L)
                .firebaseUid("superadmin")
                .email("superadmin@test.com")
                .name("Super")
                .lastName("Admin")
                .roles(Set.of(Role.SUPERADMIN, Role.ORG_ADMIN, Role.ORG_OWNER, Role.PROFESSOR, Role.CLIENT))
                .active(true)
                .build();

        userRepository.userByUid = superadmin;

        AuthenticatedUser authenticatedUser = adapter.enrichWithUserData(
                "mock-superadmin",
                AuthenticatedUser.builder()
                        .firebaseUid("mock-superadmin")
                        .email("superadmin@test.com")
                        .name("Super Admin")
                        .provider(AuthProvider.EMAIL)
                        .emailVerified(true)
        ).build();

        assertEquals(1L, authenticatedUser.getUserId());
        assertTrue(authenticatedUser.hasRole(Role.SUPERADMIN));
        assertTrue(authenticatedUser.isActive());
    }

    private static class StubUserRepository implements UserRepository {
        User userByUid;

        @Override
        public Optional<User> findByFirebaseUid(String firebaseUid) {
            if (userByUid == null) {
                return Optional.empty();
            }
            if (firebaseUid.equals(userByUid.getFirebaseUid())) {
                return Optional.of(userByUid);
            }
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public User save(User user) {
            return user;
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public void deleteById(Long id) {
        }
    }

    private static class StubHeadquartersRepository implements HeadquartersRepository {
        private final java.util.Map<Long, Headquarters> headquartersById = new java.util.HashMap<>();

        @Override
        public Headquarters save(Headquarters headquarters) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Headquarters> findById(Long id) {
            return Optional.ofNullable(headquartersById.get(id));
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
            return headquartersById.containsKey(id);
        }
    }

    private static class StubOrganizationRepository implements OrganizationRepository {
        private final java.util.Map<Long, Organization> organizationsById = new java.util.HashMap<>();

        @Override
        public Organization save(Organization organization) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Organization> findById(Long id) {
            return Optional.ofNullable(organizationsById.get(id));
        }

        @Override
        public List<Organization> findAll() {
            return List.of();
        }

        @Override
        public void deleteById(Long id) {
        }

        @Override
        public boolean existsById(Long id) {
            return organizationsById.containsKey(id);
        }
    }
}
