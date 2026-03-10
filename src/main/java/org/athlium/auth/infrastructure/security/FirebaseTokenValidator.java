package org.athlium.auth.infrastructure.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.TokenValidator;
import org.athlium.auth.domain.exception.InvalidTokenException;
import org.athlium.auth.domain.exception.TokenExpiredException;
import org.athlium.auth.domain.model.AuthProvider;
import org.athlium.auth.domain.model.DecodedToken;
import org.athlium.auth.infrastructure.config.FirebaseConfig;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Firebase implementation of TokenValidator.
 * Validates Firebase ID tokens using the Admin SDK.
 */
@ApplicationScoped
public class FirebaseTokenValidator implements TokenValidator {

    private static final Logger LOG = Logger.getLogger(FirebaseTokenValidator.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    FirebaseConfig firebaseConfig;

    @Override
    public DecodedToken validateToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw InvalidTokenException.missingToken();
        }

        String token = normalizeToken(idToken);

        if (firebaseConfig.isMockEnabled()) {
            return createMockToken(token);
        }

        FirebaseAuth firebaseAuth = firebaseConfig.getFirebaseAuth();
        if (firebaseAuth == null) {
            throw new InvalidTokenException("Firebase is not initialized");
        }

        try {
            FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);
            return mapToDecodedToken(firebaseToken);
        } catch (FirebaseAuthException e) {
            LOG.warn("Firebase token validation failed: " + e.getMessage());
            handleFirebaseException(e);
            throw InvalidTokenException.malformed(); // Fallback
        }
    }

    @Override
    public boolean isReady() {
        return firebaseConfig.isInitialized();
    }

    private DecodedToken mapToDecodedToken(FirebaseToken firebaseToken) {
        // Get provider from sign_in_provider claim
        String providerId = "email";
        
        try {
            Object firebaseClaim = firebaseToken.getClaims().get("firebase");
            if (firebaseClaim instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> firebaseClaims = (java.util.Map<String, Object>) firebaseClaim;
                Object signInProvider = firebaseClaims.get("sign_in_provider");
                if (signInProvider != null) {
                    providerId = signInProvider.toString();
                }
            }
        } catch (Exception e) {
            LOG.warn("Could not extract sign_in_provider from token claims: " + e.getMessage());
        }

        return DecodedToken.builder()
                .uid(firebaseToken.getUid())
                .email(firebaseToken.getEmail())
                .name(firebaseToken.getName())
                .picture(firebaseToken.getPicture())
                .emailVerified(firebaseToken.isEmailVerified())
                .provider(AuthProvider.fromProviderId(providerId))
                .issuedAt(Instant.ofEpochSecond(firebaseToken.getClaims().get("iat") != null 
                        ? ((Number) firebaseToken.getClaims().get("iat")).longValue() 
                        : 0))
                .expiresAt(Instant.ofEpochSecond(firebaseToken.getClaims().get("exp") != null 
                        ? ((Number) firebaseToken.getClaims().get("exp")).longValue() 
                        : 0))
                .build();
    }

    private void handleFirebaseException(FirebaseAuthException e) {
        String errorCode = e.getAuthErrorCode() != null 
                ? e.getAuthErrorCode().name() 
                : "UNKNOWN";

        switch (errorCode) {
            case "EXPIRED_ID_TOKEN":
                throw new TokenExpiredException();
            case "INVALID_ID_TOKEN":
            case "REVOKED_ID_TOKEN":
                throw InvalidTokenException.malformed();
            default:
                throw new InvalidTokenException("Token validation failed: " + errorCode, e);
        }
    }

    private String normalizeToken(String tokenValue) {
        String normalized = tokenValue == null ? "" : tokenValue.trim();

        if (normalized.isEmpty()) {
            throw InvalidTokenException.missingToken();
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.startsWith("bearer")) {
            String withoutPrefix = normalized.substring(6).trim();
            if (withoutPrefix.isEmpty()) {
                throw InvalidTokenException.missingToken();
            }
            return withoutPrefix;
        }

        return normalized;
    }

    /**
     * Creates a mock token for development/testing.
     * The token string is used as the UID for simplicity.
     */
    private DecodedToken createMockToken(String token) {
        LOG.warn("Using mock token validation - DO NOT USE IN PRODUCTION");

        Optional<Map<String, Object>> jwtClaims = parseJwtClaims(token);

        String tokenUid = jwtClaims
                .map(this::extractUidFromClaims)
                .filter(uid -> !uid.isBlank())
                .orElseGet(() -> token.length() > 20 ? token.substring(0, 20) : token);

        String email = jwtClaims
                .map(claims -> claims.get("email"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElse("mock-user@example.com");

        String name = jwtClaims
                .map(claims -> claims.get("name"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElse("Mock User");

        return DecodedToken.builder()
                .uid("mock-" + tokenUid)
                .email(email)
                .name(name)
                .picture(null)
                .emailVerified(true)
                .provider(AuthProvider.EMAIL)
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private Optional<Map<String, Object>> parseJwtClaims(String token) {
        if (token == null) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        try {
            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> claims = OBJECT_MAPPER.readValue(decodedPayload, new TypeReference<>() {
            });
            return Optional.of(claims);
        } catch (Exception e) {
            LOG.debug("Could not parse JWT claims in mock mode: " + e.getMessage());
            return Optional.empty();
        }
    }

    private String extractUidFromClaims(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            return "";
        }

        Object userId = claims.get("user_id");
        if (userId instanceof String uid && !uid.isBlank()) {
            return uid;
        }

        Object sub = claims.get("sub");
        if (sub instanceof String uid && !uid.isBlank()) {
            return uid;
        }

        return "";
    }
}
