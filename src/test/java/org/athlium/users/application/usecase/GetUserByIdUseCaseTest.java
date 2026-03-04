package org.athlium.users.application.usecase;

import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.users.domain.model.PackageStatus;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.model.UserHqMembership;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.domain.repository.UserQueryRepository;
import org.athlium.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GetUserByIdUseCaseTest {

    private GetUserByIdUseCase useCase;
    private InMemoryUserQueryRepository queryRepository;
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        useCase = new GetUserByIdUseCase();
        queryRepository = new InMemoryUserQueryRepository();
        userRepository = new InMemoryUserRepository();
        useCase.userQueryRepository = queryRepository;
        useCase.userRepository = userRepository;
    }

    @Test
    void shouldReturnUserWithPackageStatus() {
        UserWithPackageStatus expected = createTestUserWithStatus(1L, "Alice", PackageStatus.ACTIVE);
        queryRepository.setFindUserByIdResult(expected);

        UserWithPackageStatus result = useCase.execute(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alice", result.getName());
        assertEquals(PackageStatus.ACTIVE, result.getPackageStatus());
    }

    @Test
    void shouldReturnNoPackageStatusWhenUserExistsButNoPackages() {
        // userQueryRepo returns null (no packages)
        queryRepository.setFindUserByIdResult(null);

        // userRepo has the user
        User user = User.builder()
                .id(1L)
                .name("Bob")
                .lastName("Smith")
                .email("bob@test.com")
                .roles(Set.of(Role.CLIENT))
                .active(true)
                .build();
        userRepository.setFindByIdResult(user);

        UserWithPackageStatus result = useCase.execute(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Bob", result.getName());
        assertEquals(PackageStatus.NO_PACKAGE, result.getPackageStatus());
        assertNull(result.getPeriodEnd());
        assertNull(result.getDaysRemaining());
    }

    @Test
    void shouldThrowEntityNotFoundWhenUserDoesNotExist() {
        queryRepository.setFindUserByIdResult(null);
        userRepository.setFindByIdResult(null);

        assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(999L));
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute(null));
    }

    private static UserWithPackageStatus createTestUserWithStatus(Long id, String name, PackageStatus status) {
        UserWithPackageStatus user = new UserWithPackageStatus();
        user.setId(id);
        user.setName(name);
        user.setLastName("Test");
        user.setEmail(name.toLowerCase() + "@test.com");
        user.setActive(true);
        user.setPackageStatus(status);
        user.setPeriodEnd(LocalDate.now().plusDays(30));
        user.setDaysRemaining(30);
        return user;
    }

    static class InMemoryUserQueryRepository implements UserQueryRepository {
        private UserWithPackageStatus findByIdResult;

        void setFindUserByIdResult(UserWithPackageStatus result) {
            this.findByIdResult = result;
        }

        @Override
        public PageResponse<UserWithPackageStatus> findUsersByHeadquarters(Long headquartersId, String status,
                String search, int page, int size, String sort) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public PageResponse<UserWithPackageStatus> findUsersByOrganization(Long organizationId, String status,
                String search, int page, int size, String sort) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public List<UserHqMembership> findHqMembershipsByUserIds(List<Long> userIds, Long organizationId) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public PageResponse<UserWithPackageStatus> findAllUsers(String status, String search,
                int page, int size, String sort) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public UserWithPackageStatus findUserById(Long userId) {
            return findByIdResult;
        }
    }

    static class InMemoryUserRepository implements UserRepository {
        private User findByIdResult;

        void setFindByIdResult(User user) {
            this.findByIdResult = user;
        }

        @Override
        public Optional<User> findByFirebaseUid(String firebaseUid) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public Optional<User> findByEmail(String email) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public User save(User user) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(findByIdResult);
        }

        @Override
        public void deleteById(Long id) {
            throw new UnsupportedOperationException("Not used in this test");
        }
    }
}
