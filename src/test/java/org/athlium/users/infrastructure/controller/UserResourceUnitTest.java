package org.athlium.users.infrastructure.controller;

import jakarta.ws.rs.core.Response;
import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.domain.model.AuthenticatedUser;
import org.athlium.auth.infrastructure.security.SecurityContext;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.users.application.usecase.GetUserByUidUseCase;
import org.athlium.users.application.usecase.UpdateUserRolesUseCase;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;
import org.athlium.users.infrastructure.dto.UpdateRolesRequestDto;
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

    @BeforeEach
    void setUp() {
        resource = new UserResource();
        updateUserRolesUseCase = new StubUpdateUserRolesUseCase();

        resource.updateUserRolesUseCase = updateUserRolesUseCase;
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
                    .active(user.getActive())
                    .build();
        }
    }
}
