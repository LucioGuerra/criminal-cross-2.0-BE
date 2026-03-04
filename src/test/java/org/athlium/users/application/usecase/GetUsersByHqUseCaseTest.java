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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GetUsersByHqUseCaseTest {

    private GetUsersByHqUseCase useCase;
    private InMemoryUserQueryRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new GetUsersByHqUseCase();
        repository = new InMemoryUserQueryRepository();
        useCase.userQueryRepository = repository;
    }

    @Test
    void shouldReturnUsersForValidInput() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));
        repository.addUser(createTestUser(2L, "Bob", PackageStatus.EXPIRING));

        PageResponse<UserWithPackageStatus> result = useCase.execute(1L, null, null, 1, 20, null);

        assertEquals(2, result.getContent().size());
        assertEquals(1L, repository.lastHeadquartersId);
        assertEquals(0, repository.lastPage); // 1-indexed → 0-indexed
        assertEquals(20, repository.lastSize);
    }

    @Test
    void shouldThrowWhenHeadquartersIdIsNull() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute(null, null, null, 1, 20, null));
    }

    @Test
    void shouldThrowWhenPageIsLessThanOne() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute(1L, null, null, 0, 20, null));
    }

    @Test
    void shouldThrowWhenSizeIsZero() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute(1L, null, null, 1, 0, null));
    }

    @Test
    void shouldThrowWhenSizeExceedsMax() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute(1L, null, null, 1, 101, null));
    }

    @Test
    void shouldThrowWhenStatusIsInvalid() {
        assertThrows(BadRequestException.class,
                () -> useCase.execute(1L, "INVALID", null, 1, 20, null));
    }

    @Test
    void shouldAcceptValidStatusFilter() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));

        PageResponse<UserWithPackageStatus> result = useCase.execute(1L, "ACTIVE", null, 1, 20, null);

        assertNotNull(result);
        assertEquals("ACTIVE", repository.lastStatus);
    }

    @Test
    void shouldDefaultSortToNameAsc() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));

        useCase.execute(1L, null, null, 1, 20, null);

        assertEquals("name:asc", repository.lastSort);
    }

    @Test
    void shouldPassSearchToRepository() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));

        useCase.execute(1L, null, "alice", 1, 20, null);

        assertEquals("alice", repository.lastSearch);
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

        Long lastHeadquartersId;
        Long lastOrganizationId;
        String lastStatus;
        String lastSearch;
        int lastPage;
        int lastSize;
        String lastSort;
        boolean findHqMembershipsCalled;
        List<Long> lastUserIds;
        Long lastFindByIdUserId;
        UserWithPackageStatus findByIdResult;

        void addUser(UserWithPackageStatus user) {
            users.add(user);
        }

        void setFindUserByIdResult(UserWithPackageStatus result) {
            this.findByIdResult = result;
        }

        @Override
        public PageResponse<UserWithPackageStatus> findUsersByHeadquarters(Long headquartersId, String status,
                String search, int page, int size, String sort) {
            this.lastHeadquartersId = headquartersId;
            this.lastStatus = status;
            this.lastSearch = search;
            this.lastPage = page;
            this.lastSize = size;
            this.lastSort = sort;
            return new PageResponse<>(new ArrayList<>(users), page, size, users.size());
        }

        @Override
        public PageResponse<UserWithPackageStatus> findUsersByOrganization(Long organizationId, String status,
                String search, int page, int size, String sort) {
            this.lastOrganizationId = organizationId;
            this.lastStatus = status;
            this.lastSearch = search;
            this.lastPage = page;
            this.lastSize = size;
            this.lastSort = sort;
            return new PageResponse<>(new ArrayList<>(users), page, size, users.size());
        }

        @Override
        public List<UserHqMembership> findHqMembershipsByUserIds(List<Long> userIds, Long organizationId) {
            this.findHqMembershipsCalled = true;
            this.lastUserIds = userIds;
            this.lastOrganizationId = organizationId;
            return List.of();
        }

        @Override
        public PageResponse<UserWithPackageStatus> findAllUsers(String status, String search,
                int page, int size, String sort) {
            this.lastStatus = status;
            this.lastSearch = search;
            this.lastPage = page;
            this.lastSize = size;
            this.lastSort = sort;
            return new PageResponse<>(new ArrayList<>(users), page, size, users.size());
        }

        @Override
        public UserWithPackageStatus findUserById(Long userId) {
            this.lastFindByIdUserId = userId;
            return findByIdResult;
        }
    }
}
