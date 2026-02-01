package org.athlium.users.application.usecase;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.athlium.users.domain.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CreateUserUseCaseTest {

    @Inject
    CreateUserUseCase createUserUseCase;

    @Test
    void shouldCreateUserWithClientRole() {
        var user = createUserUseCase.execute(
                "firebase-uid-123",
                "test@example.com",
                "John",
                "Doe"
        );

        assertNotNull(user);
        assertEquals("firebase-uid-123", user.getFirebaseUid());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getName());
        assertEquals("Doe", user.getLastName());
        assertTrue(user.hasRole(Role.CLIENT));
        assertTrue(user.getActive());
    }
}