package org.athlium.users.application.usecase;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.athlium.users.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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
        assertEquals("firebase-uid-123", getField(user, "firebaseUid"));
        assertEquals("test@example.com", getField(user, "email"));
        assertEquals("John", getField(user, "name"));
        assertEquals("Doe", getField(user, "lastName"));
        assertTrue(user.hasRole(Role.CLIENT));
        assertEquals(Boolean.TRUE, getField(user, "active"));
    }

    private static Object getField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed reading field " + fieldName, e);
        }
    }
}
