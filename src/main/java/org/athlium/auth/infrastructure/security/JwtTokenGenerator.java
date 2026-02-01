package org.athlium.auth.infrastructure.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Generates custom JWTs signed by the backend.
 * These JWTs are short-lived and contain user claims.
 */
@ApplicationScoped
public class JwtTokenGenerator {

    private static final Logger LOG = Logger.getLogger(JwtTokenGenerator.class);

    @ConfigProperty(name = "auth.access-token.expiration-minutes", defaultValue = "15")
    int accessTokenExpirationMinutes;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://athlium.com")
    String issuer;

    /**
     * Generates a signed JWT for the authenticated user.
     *
     * @param firebaseUid User's Firebase UID
     * @param email       User's email
     * @param userId      User's database ID
     * @param roles       User's roles
     * @return Signed JWT string
     */
    public String generateAccessToken(String firebaseUid, String email, Long userId, Set<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        String token = Jwt.issuer(issuer)
                .upn(email) // User Principal Name (standard claim)
                .subject(firebaseUid) // Subject = Firebase UID
                .claim("userId", userId)
                .claim("email", email)
                .claim("firebaseUid", firebaseUid)
                .groups(roles) // Roles as groups (standard claim)
                .issuedAt(now)
                .expiresAt(expiry)
                .sign();

        LOG.debugf("Generated JWT for user %s, expires at %s", email, expiry);
        return token;
    }

    /**
     * Gets the expiration time in seconds for access tokens.
     *
     * @return Expiration time in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMinutes * 60L;
    }
}
