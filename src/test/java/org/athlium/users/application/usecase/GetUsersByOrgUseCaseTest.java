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

class GetUsersByOrgUseCaseTest {

    private GetUsersByOrgUseCase useCase;
    private InMemoryUserQueryRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new GetUsersByOrgUseCase();
        repository = new InMemoryUserQueryRepository();
        useCase.userQueryRepository = repository;
    }

    @Test
    void shouldReturnUsersForValidInput() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));

        PageResponse<UserWithPackageStatus> result = useCase.execute(1L, null, null, 1, 20, null);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, repository.lastOrganizationId);
        assertEquals(0, repository.lastPage);
        assertEquals(20, repository.lastSize);
    }

    @Test
    void shouldThrowWhenOrganizationIdIsNull() {
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
    void shouldDefaultSortToNameAsc() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));

        useCase.execute(1L, null, null, 1, 20, null);

        assertEquals("name:asc", repository.lastSort);
    }

    @Test
    void shouldPassSearchToRepository() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));

        useCase.execute(1L, null, "ali", 1, 20, null);

        assertEquals("ali", repository.lastSearch);
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

        Long lastOrganizationId;
        String lastSearch;
        int lastPage;
        int lastSize;
        String lastSort;

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
            this.lastOrganizationId = organizationId;
            this.lastSearch = search;
            this.lastPage = page;
            this.lastSize = size;
            this.lastSort = sort;
            return new PageResponse<>(new ArrayList<>(users), page, size, users.size());
        }

        @Override
        public List<UserHqMembership> findHqMembershipsByUserIds(List<Long> userIds) {
            return List.of();
        }

        @Override
        public PageResponse<UserWithPackageStatus> findAllUsers(String status, String search,
                int page, int size, String sort) {
            return new PageResponse<>(new ArrayList<>(users), page, size, users.size());
        }

        @Override
        public UserWithPackageStatus findUserById(Long userId) {
            return null;
        }
    }
}
