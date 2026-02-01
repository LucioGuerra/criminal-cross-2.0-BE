package org.athlium.auth.infrastructure.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Set;
import java.util.HashSet;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

/**
 * Validates custom JWT tokens issued by the backend.
 */
@ApplicationScoped
public class CustomJwtValidator {

    private static final Logger LOG = Logger.getLogger(CustomJwtValidator.class);

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.publickey.location")
    String publicKeyLocation;

    private PublicKey publicKey;

    /**
     * Result of JWT validation.
     */
    public record JwtValidationResult(
            String firebaseUid,
            String email,
            Long userId,
            Set<String> roles
    ) {}

    /**
     * Validates a custom JWT token and extracts user information.
     *
     * @param token The JWT token string (with or without "Bearer " prefix)
     * @return Validation result with user data
     * @throws org.athlium.auth.domain.exception.AuthenticationException if token is invalid
     */
    public JwtValidationResult validateCustomJwt(String token) {
        try {
            // Remove "Bearer " prefix if present
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            // Lazy load public key
            if (publicKey == null) {
                publicKey = loadPublicKey();
            }

            // Parse and validate JWT with signature verification
            Claims claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .requireIssuer(issuer)
                    .parseClaimsJws(cleanToken)
                    .getBody();

            // Extract claims
            String firebaseUid = claims.getSubject();
            String email = claims.get("upn", String.class);
            Long userId = claims.get("userId", Long.class);
            @SuppressWarnings("unchecked")
            java.util.List<String> groupsList = claims.get("groups", java.util.List.class);
            Set<String> roles = groupsList != null ? new HashSet<>(groupsList) : new HashSet<>();

            // Validate required claims
            if (firebaseUid == null || email == null || userId == null) {
                throw new org.athlium.auth.domain.exception.AuthenticationException("Invalid JWT: missing required claims");
            }

            LOG.debugf("Custom JWT validated successfully for user: %s (ID: %d)", email, userId);

            return new JwtValidationResult(firebaseUid, email, userId, roles);

        } catch (io.jsonwebtoken.JwtException e) {
            LOG.warnf("Failed to validate custom JWT: %s", e.getMessage());
            throw new org.athlium.auth.domain.exception.AuthenticationException("Invalid JWT token: " + e.getMessage());
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error validating custom JWT");
            throw new org.athlium.auth.domain.exception.AuthenticationException("Token validation failed");
        }
    }

    /**
     * Loads the public key from the configured location.
     */
    private PublicKey loadPublicKey() throws Exception {
        // Remove leading slash if present for classpath resource
        String resourcePath = publicKeyLocation.startsWith("/") ? publicKeyLocation.substring(1) : publicKeyLocation;
        
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Public key not found at: " + resourcePath);
            }
            
            String key = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        }
    }

    /**
     * Checks if a token is a custom JWT (vs Firebase token).
     * Custom JWTs are signed by our backend and have our issuer.
     * 
     * Simple heuristic: decode the payload and check the issuer without signature verification.
     *
     * @param token The token string
     * @return true if it's a custom JWT, false if it's a Firebase token
     */
    public boolean isCustomJwt(String token) {
        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            // Split JWT into parts
            String[] parts = cleanToken.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            
            // Decode payload (base64url decode)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            
            // Check if issuer matches (simple string contains check)
            return payload.contains("\"iss\":\"" + issuer + "\"");
            
        } catch (Exception e) {
            // If parsing fails, assume it's a Firebase token
            LOG.debugf("Failed to check if custom JWT: %s", e.getMessage());
            return false;
        }
    }
}
