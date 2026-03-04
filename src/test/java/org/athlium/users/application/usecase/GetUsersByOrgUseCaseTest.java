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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    void shouldEnrichUsersWithHqMemberships() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));
        repository.addUser(createTestUser(2L, "Bob", PackageStatus.EXPIRING));

        UserHqMembership aliceMembership = createHqMembership(1L, 10L, "HQ Alpha", PackageStatus.ACTIVE);
        UserHqMembership bobMembership = createHqMembership(2L, 20L, "HQ Beta", PackageStatus.EXPIRING);
        repository.addHqMembership(aliceMembership);
        repository.addHqMembership(bobMembership);

        PageResponse<UserWithPackageStatus> result = useCase.execute(5L, null, null, 1, 20, null);

        assertTrue(repository.findHqMembershipsCalled);
        assertEquals(2, result.getContent().size());

        UserWithPackageStatus alice = result.getContent().get(0);
        assertNotNull(alice.getHqMemberships());
        assertEquals(1, alice.getHqMemberships().size());
        assertEquals("HQ Alpha", alice.getHqMemberships().get(0).getHqName());

        UserWithPackageStatus bob = result.getContent().get(1);
        assertNotNull(bob.getHqMemberships());
        assertEquals(1, bob.getHqMemberships().size());
        assertEquals("HQ Beta", bob.getHqMemberships().get(0).getHqName());
    }

    @Test
    void shouldHandleEmptyUserListWithoutCallingHqMemberships() {
        // No users added — empty page

        PageResponse<UserWithPackageStatus> result = useCase.execute(1L, null, null, 1, 20, null);

        assertTrue(result.getContent().isEmpty());
        assertFalse(repository.findHqMembershipsCalled);
    }

    @Test
    void shouldReturnEmptyHqMembershipsForUsersWithNone() {
        repository.addUser(createTestUser(1L, "Alice", PackageStatus.ACTIVE));
        // No HQ memberships added

        PageResponse<UserWithPackageStatus> result = useCase.execute(5L, null, null, 1, 20, null);

        UserWithPackageStatus alice = result.getContent().get(0);
        assertNotNull(alice.getHqMemberships());
        assertTrue(alice.getHqMemberships().isEmpty());
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

    private static UserHqMembership createHqMembership(Long userId, Long hqId, String hqName, PackageStatus status) {
        UserHqMembership membership = new UserHqMembership();
        membership.setUserId(userId);
        membership.setHqId(hqId);
        membership.setHqName(hqName);
        membership.setPackageStatus(status);
        membership.setPeriodEnd(LocalDate.now().plusDays(30));
        membership.setDaysRemaining(30);
        return membership;
    }

    static class InMemoryUserQueryRepository implements UserQueryRepository {
        private final List<UserWithPackageStatus> users = new ArrayList<>();
        private final List<UserHqMembership> hqMemberships = new ArrayList<>();

        Long lastOrganizationId;
        int lastPage;
        int lastSize;
        String lastSort;
        boolean findHqMembershipsCalled;

        void addUser(UserWithPackageStatus user) {
            users.add(user);
        }

        void addHqMembership(UserHqMembership membership) {
            hqMemberships.add(membership);
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
            this.lastPage = page;
            this.lastSize = size;
            this.lastSort = sort;
            return new PageResponse<>(new ArrayList<>(users), page, size, users.size());
        }

        @Override
        public List<UserHqMembership> findHqMembershipsByUserIds(List<Long> userIds, Long organizationId) {
            this.findHqMembershipsCalled = true;
            // Return memberships whose userId is in the requested list
            List<UserHqMembership> result = new ArrayList<>();
            for (UserHqMembership m : hqMemberships) {
                if (userIds.contains(m.getUserId())) {
                    result.add(m);
                }
            }
            return result;
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
