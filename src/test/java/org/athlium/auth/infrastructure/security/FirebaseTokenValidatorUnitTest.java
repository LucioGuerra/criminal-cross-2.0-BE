package org.athlium.auth.infrastructure.security;

import org.athlium.auth.infrastructure.config.FirebaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirebaseTokenValidatorUnitTest {

    private FirebaseConfig firebaseConfig;
    FirebaseTokenValidator validator;

    @BeforeEach
    void setUp() throws Exception {
        firebaseConfig = new FirebaseConfig();
        var mockEnabledField = FirebaseConfig.class.getDeclaredField("mockEnabled");
        mockEnabledField.setAccessible(true);
        mockEnabledField.setBoolean(firebaseConfig, true);

        validator = new FirebaseTokenValidator();
        validator.firebaseConfig = firebaseConfig;
    }

    @Test
    void shouldAcceptBearerPrefixCaseInsensitively() {
        var decoded = validator.validateToken("bearer frontend-token-1");
        assertEquals("mock-frontend-token-1", decoded.getUid());
    }

    @Test
    void shouldAcceptAuthorizationValueWithExtraWhitespace() {
        var decoded = validator.validateToken("   Bearer   frontend-token-2   ");
        assertEquals("mock-frontend-token-2", decoded.getUid());
    }
}
