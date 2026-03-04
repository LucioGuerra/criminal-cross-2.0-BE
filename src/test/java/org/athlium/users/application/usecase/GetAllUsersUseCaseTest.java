package org.athlium.users.application.usecase;

import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.users.domain.model.PackageStatus;
import org.athlium.users.domain.model.UserHqMembership;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.domain.repository.UserQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetAllUsersUseCaseTest {

    private GetAllUsersUseCase useCase;
    private InMemoryUserQueryRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new GetAllUsersUseCase();
        repository = new InMemoryUserQueryRepository();
        useCase.userQueryRepository = repository;
    }

    @Test
    void shouldReturnAllUsersForValidInput() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));
        repository.addUser(createTestUser(2L, "Bob", PackageStatus.EXPIRING));

        PageResponse<UserWithPackageStatus> result = useCase.execute(null, null, 1, 20, null);

        assertEquals(2, result.getContent().size());
        assertEquals(0, repository.lastPage); // 1-indexed → 0-indexed
        assertEquals(20, repository.lastSize);
        assertTrue(repository.findAllUsersCalled);
    }

    @Test
    void shouldThrowWhenPageIsLessThanOne() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute(null, null, 0, 20, null));
    }

    @Test
    void shouldThrowWhenSizeIsZero() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute(null, null, 1, 0, null));
    }

    @Test
    void shouldThrowWhenSizeExceedsMax() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute(null, null, 1, 101, null));
    }

    @Test
    void shouldThrowWhenStatusIsInvalid() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute("INVALID", null, 1, 20, null));
    }

    @Test
    void shouldPassStatusFilterToRepository() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.NO_PACKAGE));

        useCase.execute("NO_PACKAGE", null, 1, 20, null);

        assertEquals("NO_PACKAGE", repository.lastStatus);
    }

    @Test
    void shouldPassSearchToRepository() {
        repository.addUser(createTestUser(1L, "John", PackageStatus.ACTIVE));

        useCase.execute(null, "john", 1, 20, null);

        assertEquals("john", repository.lastSearch);
    }

    @Test
    void shouldDefaultSortToNameAsc() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));

        useCase.execute(null, null, 1, 20, null);

        assertEquals("name:asc", repository.lastSort);
    }

    private static UserWithPackageStatus createTestUser(Long id, String name, PackageStatus status) {
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
        private final List<UserWithPackageStatus> users = new ArrayList<>();

        String lastStatus;
        String lastSearch;
        int lastPage;
        int lastSize;
        String lastSort;
        boolean findAllUsersCalled;

        void addUser(UserWithPackageStatus user) {
            users.add(user);
        }

        @Override
        public PageResponse<UserWithPackageStatus> findUsersByHeadquarters(Long headquartersId, String status,
                String search, int page, int size, String sort) {
            return new PageResponse<>(new ArrayList<>(users), page, size, users.size());
        }

        @Override
        public PageResponse<UserWithPackageStatus> findUsersByOrganization(Long organizationId, String status,
                String search, int page, int size, String sort) {
            return new PageResponse<>(new ArrayList<>(users), page, size, users.size());
        }

        @Override
        public List<UserHqMembership> findHqMembershipsByUserIds(List<Long> userIds, Long organizationId) {
            return List.of();
        }

        @Override
        public PageResponse<UserWithPackageStatus> findAllUsers(String status, String search,
                int page, int size, String sort) {
            this.findAllUsersCalled = true;
            this.lastStatus = status;
            this.lastSearch = search;
            this.lastPage = page;
            this.lastSize = size;
            this.lastSort = sort;
            return new PageResponse<>(new ArrayList<>(users), page, size, users.size());
        }

        @Override
        public UserWithPackageStatus findUserById(Long userId) {
            return null;
        }
    }
}
