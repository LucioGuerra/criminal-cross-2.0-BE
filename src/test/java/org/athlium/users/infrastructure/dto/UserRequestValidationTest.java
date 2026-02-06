package org.athlium.users.infrastructure.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.athlium.users.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldFailWhenCreateUserRequestIsInvalid() {
        CreateUserRequestDto request = new CreateUserRequestDto();
        setField(request, "firebaseUid", "");
        setField(request, "email", "not-an-email");
        setField(request, "name", "");
        setField(request, "lastName", "");

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenUpdateRolesRequestHasNullRole() {
        UpdateRolesRequestDto request = new UpdateRolesRequestDto();
        Set<Role> roles = new HashSet<>();
        roles.add(null);
        setField(request, "roles", roles);

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassWhenRequestsAreValid() {
        CreateUserRequestDto createRequest = new CreateUserRequestDto();
        setField(createRequest, "firebaseUid", "uid-1");
        setField(createRequest, "email", "john@doe.com");
        setField(createRequest, "name", "John");
        setField(createRequest, "lastName", "Doe");

        UpdateRolesRequestDto rolesRequest = new UpdateRolesRequestDto();
        setField(rolesRequest, "roles", Set.of(Role.CLIENT));

        var createViolations = validator.validate(createRequest);
        var roleViolations = validator.validate(rolesRequest);

        assertTrue(createViolations.isEmpty());
        assertTrue(roleViolations.isEmpty());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed setting field " + fieldName, e);
        }
    }
}
