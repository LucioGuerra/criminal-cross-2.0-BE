package org.athlium.auth.infrastructure.security;

import org.athlium.auth.infrastructure.config.FirebaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

    @Test
    void shouldExtractFirebaseUidFromJwtClaimsInMockMode() {
        String header = encode("{\"alg\":\"RS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"user_id\":\"superadmin\",\"sub\":\"sub-fallback\",\"email\":\"superadmin@test.com\",\"name\":\"Super Admin\"}");
        String token = header + "." + payload + ".signature";

        var decoded = validator.validateToken("Bearer " + token);

        assertEquals("mock-superadmin", decoded.getUid());
        assertEquals("superadmin@test.com", decoded.getEmail());
        assertEquals("Super Admin", decoded.getName());
    }

    private String encode(String json) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
