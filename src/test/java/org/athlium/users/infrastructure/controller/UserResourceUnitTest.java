package org.athlium.users.infrastructure.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.security.SecurityContext;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.users.application.usecase.GetUserByUidUseCase;
import org.athlium.users.application.usecase.AssignUserToHeadquartersUseCase;
import org.athlium.users.application.usecase.RemoveUserFromHeadquartersUseCase;
import org.athlium.users.application.usecase.UpdateUserUseCase;
import org.athlium.users.application.usecase.UpdateUserRolesUseCase;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.infrastructure.dto.UpdateRolesRequestDto;
import org.athlium.users.infrastructure.dto.CreateUserRequestDto;
import org.athlium.users.infrastructure.dto.UpdateUserRequestDto;
import org.athlium.users.infrastructure.dto.UserResponseDto;
import org.athlium.users.infrastructure.mapper.UserDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserResourceUnitTest {

    private UserResource resource;
    private StubUpdateUserRolesUseCase updateUserRolesUseCase;
    private StubUpdateUserUseCase updateUserUseCase;
    private StubAssignUserToHeadquartersUseCase assignUserToHeadquartersUseCase;
    private StubRemoveUserFromHeadquartersUseCase removeUserFromHeadquartersUseCase;

    @BeforeEach
    void setUp() {
        resource = new UserResource();
        updateUserRolesUseCase = new StubUpdateUserRolesUseCase();
        updateUserUseCase = new StubUpdateUserUseCase();
        assignUserToHeadquartersUseCase = new StubAssignUserToHeadquartersUseCase();
        removeUserFromHeadquartersUseCase = new StubRemoveUserFromHeadquartersUseCase();

        resource.updateUserRolesUseCase = updateUserRolesUseCase;
        resource.updateUserUseCase = updateUserUseCase;
        resource.assignUserToHeadquartersUseCase = assignUserToHeadquartersUseCase;
        resource.removeUserFromHeadquartersUseCase = removeUserFromHeadquartersUseCase;
        resource.getUserByUidUseCase = new StubGetUserByUidUseCase();
        resource.userDtoMapper = new StubUserDtoMapper();
        resource.securityContext = new SecurityContext();
        resource.authBypassEnabled = true;

        resource.securityContext.setCurrentUser(AuthenticatedUser.builder()
                .firebaseUid("dev-bypass-user")
                .email("dev-bypass@local")
                .name("Dev Bypass User")
                .provider(AuthProvider.EMAIL)
                .emailVerified(true)
                .roles(EnumSet.allOf(Role.class))
                .active(true)
                .build());
        resource.securityContext.setAuthenticated(true);
    }

    @Test
    void shouldUpdateUserRolesInBypassModeWithoutPersistedCurrentUser() {
        UpdateRolesRequestDto request = new UpdateRolesRequestDto(Set.of(Role.CLIENT));

        Response response = resource.updateUserRoles("target-firebase-uid", request);

        assertEquals(200, response.getStatus());
        assertTrue(updateUserRolesUseCase.currentUser.hasRole(Role.SUPERADMIN));

        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
    }

    @Test
    void shouldUpdateUserInBypassModeWithoutPersistedCurrentUser() {
        UpdateUserRequestDto request = new UpdateUserRequestDto("target@updated.com", "Target", "Updated", true);

        Response response = resource.updateUserById(10L, request);

        assertEquals(200, response.getStatus());
        assertTrue(updateUserUseCase.currentUser.hasRole(Role.SUPERADMIN));

        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
    }

    @Test
    void shouldAssignUserToHeadquartersInBypassModeWithoutPersistedCurrentUser() {
        Response response = resource.assignUserToHeadquarters("target-firebase-uid", 11L);

        assertEquals(200, response.getStatus());
        assertTrue(assignUserToHeadquartersUseCase.currentUser.hasRole(Role.SUPERADMIN));

        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
    }

    @Test
    void shouldRemoveUserFromHeadquartersInBypassModeWithoutPersistedCurrentUser() {
        Response response = resource.removeUserFromHeadquarters("target-firebase-uid", 11L);

        assertEquals(200, response.getStatus());
        assertTrue(removeUserFromHeadquartersUseCase.currentUser.hasRole(Role.SUPERADMIN));

        ApiResponse<?> body = (ApiResponse<?>) response.getEntity();
        assertTrue(body.isSuccess());
    }

    @Test
    void shouldReturnGoneForDeprecatedSyncEndpoint() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        Response response = resource.syncWithFirebase(request);

        assertEquals(410, response.getStatus());
    }

    private static class StubGetUserByUidUseCase extends GetUserByUidUseCase {
        @Override
        public Optional<User> execute(String firebaseUid) {
            return Optional.empty();
        }
    }

    private static class StubUpdateUserRolesUseCase extends UpdateUserRolesUseCase {
        User currentUser;

        @Override
        public User execute(String firebaseUid, Set<Role> newRoles, User currentUser) {
            this.currentUser = currentUser;
            return User.builder()
                    .id(10L)
                    .firebaseUid(firebaseUid)
                    .email("target@example.com")
                    .name("Target")
                    .lastName("User")
                    .roles(newRoles)
                    .headquartersIds(Set.of())
                    .active(true)
                    .build();
        }
    }

    private static class StubUpdateUserUseCase extends UpdateUserUseCase {
        User currentUser;

        @Override
        public User execute(Long userId, String email, String name, String lastName, Boolean active, User currentUser) {
            this.currentUser = currentUser;
            return User.builder()
                    .id(userId)
                    .firebaseUid("target-firebase-uid")
                    .email(email)
                    .name(name)
                    .lastName(lastName)
                    .roles(Set.of(Role.CLIENT))
                    .headquartersIds(Set.of())
                    .active(active)
                    .build();
        }
    }

    private static class StubAssignUserToHeadquartersUseCase extends AssignUserToHeadquartersUseCase {
        User currentUser;

        @Override
        public User execute(String firebaseUid, Long headquartersId, User currentUser) {
            this.currentUser = currentUser;
            return User.builder()
                    .id(10L)
                    .firebaseUid(firebaseUid)
                    .email("target@example.com")
                    .name("Target")
                    .lastName("User")
                    .roles(Set.of(Role.CLIENT))
                    .headquartersIds(Set.of(headquartersId))
                    .active(true)
                    .build();
        }
    }

    private static class StubRemoveUserFromHeadquartersUseCase extends RemoveUserFromHeadquartersUseCase {
        User currentUser;

        @Override
        public User execute(String firebaseUid, Long headquartersId, User currentUser) {
            this.currentUser = currentUser;
            return User.builder()
                    .id(10L)
                    .firebaseUid(firebaseUid)
                    .email("target@example.com")
                    .name("Target")
                    .lastName("User")
                    .roles(Set.of(Role.CLIENT))
                    .headquartersIds(Set.of())
                    .active(true)
                    .build();
        }
    }

    private static class StubUserDtoMapper implements UserDtoMapper {
        @Override
        public UserResponseDto toResponseDto(User user) {
            return UserResponseDto.builder()
                    .id(user.getId())
                    .firebaseUid(user.getFirebaseUid())
                    .email(user.getEmail())
                    .name(user.getName())
                    .lastName(user.getLastName())
                    .roles(user.getRoles())
                    .headquartersIds(user.getHeadquartersIds())
                    .active(user.getActive())
                    .build();
        }
    }
}
