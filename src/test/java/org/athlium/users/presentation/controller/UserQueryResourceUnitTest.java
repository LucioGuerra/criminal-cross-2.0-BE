package org.athlium.users.presentation.controller;

import jakarta.ws.rs.core.Response;

import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.users.application.usecase.GetAllUsersUseCase;
import org.athlium.users.application.usecase.GetUserByIdUseCase;
import org.athlium.users.application.usecase.GetUsersByHqUseCase;
import org.athlium.users.application.usecase.GetUsersByOrgUseCase;
import org.athlium.users.infrastructure.dto.UserResponseDto;
import org.athlium.users.domain.model.PackageStatus;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.presentation.mapper.UserQueryDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserQueryResourceUnitTest {

    private UserQueryResource resource;
    private StubGetUsersByHqUseCase hqUseCase;
    private StubGetUsersByOrgUseCase orgUseCase;
    private StubGetAllUsersUseCase allUsersUseCase;
    private StubGetUserByIdUseCase getByIdUseCase;

    @BeforeEach
    void setUp() {
        resource = new UserQueryResource();
        hqUseCase = new StubGetUsersByHqUseCase();
        orgUseCase = new StubGetUsersByOrgUseCase();
        allUsersUseCase = new StubGetAllUsersUseCase();
        getByIdUseCase = new StubGetUserByIdUseCase();

        resource.getUsersByHqUseCase = hqUseCase;
        resource.getUsersByOrgUseCase = orgUseCase;
        resource.getAllUsersUseCase = allUsersUseCase;
        resource.getUserByIdUseCase = getByIdUseCase;
        resource.userQueryDtoMapper = new FakeUserQueryDtoMapper();
    }

    // --- Tests for getUsers (GET /api/users) ---

    @Test
    void shouldReturn200WithUsersFilteredByHq() {
        hqUseCase.setResult(new PageResponse<>(
                List.of(createTestUser(1L, "Alice")), 0, 20, 1));

        Response response = resource.getUsers(1L, null, null, null, 1, 20, "name:asc");

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("Users retrieved", body.getMessage());
        assertTrue(hqUseCase.executeCalled);
        assertFalse(orgUseCase.executeCalled);
        assertFalse(allUsersUseCase.executeCalled);
    }

    @Test
    void shouldPassSearchToHqUseCase() {
        hqUseCase.setResult(new PageResponse<>(
                List.of(createTestUser(1L, "Alice")), 0, 20, 1));

        resource.getUsers(1L, null, null, "ali", 1, 20, "name:asc");

        assertEquals("ali", hqUseCase.lastSearch);
    }

    @Test
    void shouldReturn200WithUsersFilteredByHeadquartetsIdAlias() {
        hqUseCase.setResult(new PageResponse<>(
                List.of(createTestUser(1L, "Alice")), 0, 20, 1));

        Response response = resource.getUsers(null, 1L, null, null, null, 1, 20, "name:asc");

        assertEquals(200, response.getStatus());
        assertTrue(hqUseCase.executeCalled);
        assertFalse(orgUseCase.executeCalled);
        assertFalse(allUsersUseCase.executeCalled);
    }

    @Test
    void shouldReturn400WhenHeadquartersIdAndHeadquartetsIdDiffer() {
        Response response = resource.getUsers(1L, 2L, null, null, null, 1, 20, "name:asc");

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
        assertEquals("Query params headquartersId and headquartetsId must match when both are provided", body.getMessage());
    }

    @Test
    void shouldReturn200WithUsersFilteredByOrg() {
        orgUseCase.setResult(new PageResponse<>(
                List.of(createTestUser(2L, "Bob")), 0, 20, 1));

        Response response = resource.getUsers(null, 1L, null, null, 1, 20, "name:asc");

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertFalse(hqUseCase.executeCalled);
        assertTrue(orgUseCase.executeCalled);
        assertFalse(allUsersUseCase.executeCalled);
    }

    @Test
    void shouldPassSearchToOrgUseCase() {
        orgUseCase.setResult(new PageResponse<>(
                List.of(createTestUser(2L, "Bob")), 0, 20, 1));

        resource.getUsers(null, 1L, null, "bo", 1, 20, "name:asc");

        assertEquals("bo", orgUseCase.lastSearch);
    }

    @Test
    void shouldReturn200WithAllUsers() {
        allUsersUseCase.setResult(new PageResponse<>(
                List.of(createTestUser(3L, "Charlie")), 0, 20, 1));

        Response response = resource.getUsers(null, null, null, null, 1, 20, "name:asc");

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertFalse(hqUseCase.executeCalled);
        assertFalse(orgUseCase.executeCalled);
        assertTrue(allUsersUseCase.executeCalled);
    }

    @Test
    void shouldPassSearchToAllUsersUseCase() {
        allUsersUseCase.setResult(new PageResponse<>(
                List.of(createTestUser(3L, "Charlie")), 0, 20, 1));

        resource.getUsers(null, null, null, "char", 1, 20, "name:asc");

        assertEquals("char", allUsersUseCase.lastSearch);
    }

    @Test
    void shouldReturn400WhenBothHqAndOrgProvided() {
        Response response = resource.getUsers(1L, 1L, null, null, 1, 20, "name:asc");

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
        assertEquals("Cannot filter by both headquartersId and organizationId", body.getMessage());
    }

    @Test
    void shouldReturn400WhenUseCaseThrowsBadRequest() {
        hqUseCase.setException(new BadRequestException("Page must be >= 1"));

        Response response = resource.getUsers(1L, null, null, null, 0, 20, "name:asc");

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
        assertEquals("Page must be >= 1", body.getMessage());
    }

    @Test
    void shouldReturn500WhenUnexpectedError() {
        hqUseCase.setException(new RuntimeException("Database connection lost"));

        Response response = resource.getUsers(1L, null, null, null, 1, 20, "name:asc");

        assertEquals(500, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
        assertEquals("Unexpected error", body.getMessage());
    }

    // --- Tests for getUserById (GET /api/users/{id}) ---

    @Test
    void shouldReturn200WithUserById() {
        getByIdUseCase.setResult(createTestUser(1L, "Alice"));

        Response response = resource.getUserById(1L);

        assertEquals(200, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
        assertEquals("User found", body.getMessage());
    }

    @Test
    void shouldReturn404WhenUserNotFound() {
        getByIdUseCase.setException(new EntityNotFoundException("User", 999L));

        Response response = resource.getUserById(999L);

        assertEquals(404, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
    }

    @Test
    void shouldReturn400WhenInvalidId() {
        getByIdUseCase.setException(new BadRequestException("User ID must not be null"));

        Response response = resource.getUserById(null);

        assertEquals(400, response.getStatus());
        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertFalse(body.isSuccess());
    }

    // --- Helper methods ---

    private static UserWithPackageStatus createTestUser(Long id, String name) {
        UserWithPackageStatus user = new UserWithPackageStatus();
        user.setId(id);
        user.setName(name);
        user.setLastName("Test");
        user.setEmail(name.toLowerCase() + "@test.com");
        user.setActive(true);
        user.setPackageStatus(PackageStatus.ACTIVE);
        user.setPeriodEnd(LocalDate.now().plusDays(30));
        user.setDaysRemaining(30);
        return user;
    }

    // --- Stubs and Fakes ---

    static class StubGetUsersByHqUseCase extends GetUsersByHqUseCase {
        private PageResponse<UserWithPackageStatus> result;
        private RuntimeException exception;
        String lastSearch;
        boolean executeCalled;

        void setResult(PageResponse<UserWithPackageStatus> result) {
            this.result = result;
        }

        void setException(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public PageResponse<UserWithPackageStatus> execute(Long headquartersId, String status,
                String search, int page, int size, String sort) {
            executeCalled = true;
            lastSearch = search;
            if (exception != null) throw exception;
            return result;
        }
    }

    static class StubGetUsersByOrgUseCase extends GetUsersByOrgUseCase {
        private PageResponse<UserWithPackageStatus> result;
        private RuntimeException exception;
        String lastSearch;
        boolean executeCalled;

        void setResult(PageResponse<UserWithPackageStatus> result) {
            this.result = result;
        }

        void setException(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public PageResponse<UserWithPackageStatus> execute(Long organizationId, String status,
                String search, int page, int size, String sort) {
            executeCalled = true;
            lastSearch = search;
            if (exception != null) throw exception;
            return result;
        }
    }

    static class StubGetAllUsersUseCase extends GetAllUsersUseCase {
        private PageResponse<UserWithPackageStatus> result;
        private RuntimeException exception;
        String lastSearch;
        boolean executeCalled;

        void setResult(PageResponse<UserWithPackageStatus> result) {
            this.result = result;
        }

        void setException(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public PageResponse<UserWithPackageStatus> execute(String status, String search,
                int page, int size, String sort) {
            executeCalled = true;
            lastSearch = search;
            if (exception != null) throw exception;
            return result;
        }
    }

    static class StubGetUserByIdUseCase extends GetUserByIdUseCase {
        private UserWithPackageStatus result;
        private RuntimeException exception;

        void setResult(UserWithPackageStatus result) {
            this.result = result;
        }

        void setException(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public UserWithPackageStatus execute(Long userId) {
            if (exception != null) throw exception;
            return result;
        }
    }

    static class FakeUserQueryDtoMapper extends UserQueryDtoMapper {
        @Override
        public UserResponseDto toResponse(UserWithPackageStatus domain) {
            if (domain == null) return null;
            return UserResponseDto.builder()
                    .id(domain.getId())
                    .name(domain.getName())
                    .build();
        }

        @Override
        public List<UserResponseDto> toResponseList(List<UserWithPackageStatus> domains) {
            if (domains == null) return null;
            List<UserResponseDto> responses = new ArrayList<>();
            for (UserWithPackageStatus domain : domains) {
                responses.add(toResponse(domain));
            }
            return responses;
        }
    }
}
