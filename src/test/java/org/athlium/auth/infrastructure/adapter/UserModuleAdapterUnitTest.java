package org.athlium.auth.infrastructure.adapter;

import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.users.application.usecase.CreateUserUseCase;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserModuleAdapterUnitTest {

    private UserModuleAdapter adapter;
    private StubUserRepository userRepository;

    @BeforeEach
    void setUp() {
        adapter = new UserModuleAdapter();
        userRepository = new StubUserRepository();

        adapter.userRepository = userRepository;
        adapter.createUserUseCase = new CreateUserUseCase();
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
}
