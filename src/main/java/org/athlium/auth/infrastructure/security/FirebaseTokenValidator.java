package org.athlium.auth.infrastructure.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
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

/**
 * Firebase implementation of TokenValidator.
 * Validates Firebase ID tokens using the Admin SDK.
 */
@ApplicationScoped
public class FirebaseTokenValidator implements TokenValidator {

    private static final Logger LOG = Logger.getLogger(FirebaseTokenValidator.class);

    @Inject
    FirebaseConfig firebaseConfig;

    @Override
    public DecodedToken validateToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw InvalidTokenException.missingToken();
        }

        // Remove "Bearer " prefix if present
        String token = idToken.startsWith("Bearer ") 
                ? idToken.substring(7) 
                : idToken;

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

    /**
     * Creates a mock token for development/testing.
     * The token string is used as the UID for simplicity.
     */
    private DecodedToken createMockToken(String token) {
        LOG.warn("Using mock token validation - DO NOT USE IN PRODUCTION");
        
        // For mock mode, use the token itself as UID or parse it
        String uid = token.length() > 20 ? token.substring(0, 20) : token;
        
        return DecodedToken.builder()
                .uid("mock-" + uid)
                .email("mock-user@example.com")
                .name("Mock User")
                .picture(null)
                .emailVerified(true)
                .provider(AuthProvider.EMAIL)
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
