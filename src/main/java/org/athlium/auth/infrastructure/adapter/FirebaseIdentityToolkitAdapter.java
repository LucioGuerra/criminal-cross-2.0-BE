package org.athlium.auth.infrastructure.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.auth.application.ports.FirebaseIdentityProvider;
import org.athlium.auth.domain.exception.AuthenticationException;
import org.athlium.auth.domain.exception.InvalidRefreshTokenException;
import org.athlium.auth.domain.exception.UserAlreadyExistsException;
import org.athlium.auth.domain.model.FirebaseSessionTokens;
import org.athlium.auth.infrastructure.config.FirebaseConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@ApplicationScoped
public class FirebaseIdentityToolkitAdapter implements FirebaseIdentityProvider {

    private static final String IDENTITY_TOOLKIT_BASE = "https://identitytoolkit.googleapis.com/v1";
    private static final String SECURE_TOKEN_BASE = "https://securetoken.googleapis.com/v1";
    private static final Gson GSON = new Gson();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Inject
    FirebaseConfig firebaseConfig;

    @ConfigProperty(name = "firebase.web-api-key")
    Optional<String> firebaseWebApiKey;

    @Override
    public FirebaseSessionTokens register(String email, String password, String displayName) {
        if (firebaseConfig.isMockEnabled()) {
            return new FirebaseSessionTokens(
                    "mock-access-token",
                    "mock-refresh-token",
                    3600L,
                    "mock-firebase-uid",
                    email
            );
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", password);
        payload.addProperty("displayName", displayName);
        payload.addProperty("returnSecureToken", true);

        JsonObject body = executeIdentityRequest(
                IDENTITY_TOOLKIT_BASE + "/accounts:signUp?key=" + firebaseWebApiKey.orElse(""),
                payload
        );

        return mapToTokens(body);
    }

    @Override
    public FirebaseSessionTokens login(String email, String password) {
        if (firebaseConfig.isMockEnabled()) {
            return new FirebaseSessionTokens(
                    "mock-access-token",
                    "mock-refresh-token",
                    3600L,
                    "mock-firebase-uid",
                    email
            );
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", password);
        payload.addProperty("returnSecureToken", true);

        JsonObject body = executeIdentityRequest(
                IDENTITY_TOOLKIT_BASE + "/accounts:signInWithPassword?key=" + firebaseWebApiKey.orElse(""),
                payload
        );

        return mapToTokens(body);
    }

    @Override
    public FirebaseSessionTokens refresh(String refreshToken) {
        if (firebaseConfig.isMockEnabled()) {
            return new FirebaseSessionTokens(
                    "mock-refreshed-access-token",
                    "mock-refreshed-refresh-token",
                    3600L,
                    "mock-firebase-uid",
                    "mock-user@example.com"
            );
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("grant_type", "refresh_token");
        payload.addProperty("refresh_token", refreshToken);

        JsonObject body = executeIdentityRequest(
                SECURE_TOKEN_BASE + "/token?key=" + firebaseWebApiKey.orElse(""),
                payload
        );

        String accessToken = getStringOrNull(body, "id_token");
        String nextRefreshToken = getStringOrNull(body, "refresh_token");
        String userId = getStringOrNull(body, "user_id");
        long expiresIn = parseExpiresIn(getStringOrNull(body, "expires_in"));

        return new FirebaseSessionTokens(accessToken, nextRefreshToken, expiresIn, userId, null);
    }

    private JsonObject executeIdentityRequest(String url, JsonObject payload) {
        if (firebaseWebApiKey.isEmpty() || firebaseWebApiKey.get().isBlank()) {
            throw new AuthenticationException("Firebase web API key is not configured");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return JsonParser.parseString(response.body()).getAsJsonObject();
            }
            throw mapError(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException("Firebase request interrupted", e);
        } catch (IOException e) {
            throw new AuthenticationException("Unable to communicate with Firebase", e);
        }
    }

    private RuntimeException mapError(String body) {
        String code = extractFirebaseErrorCode(body);
        if ("EMAIL_EXISTS".equals(code)) {
            return new UserAlreadyExistsException("Email is already registered in Firebase");
        }
        if ("INVALID_PASSWORD".equals(code) || "EMAIL_NOT_FOUND".equals(code) || "INVALID_LOGIN_CREDENTIALS".equals(code)) {
            return new AuthenticationException("Invalid email or password");
        }
        if ("TOKEN_EXPIRED".equals(code) || "INVALID_REFRESH_TOKEN".equals(code)) {
            return new InvalidRefreshTokenException("Invalid refresh token");
        }
        return new AuthenticationException("Firebase authentication request failed: " + code);
    }

    private String extractFirebaseErrorCode(String body) {
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            return json.getAsJsonObject("error").get("message").getAsString();
        } catch (Exception ignored) {
            return "UNKNOWN_ERROR";
        }
    }

    private FirebaseSessionTokens mapToTokens(JsonObject body) {
        String accessToken = getStringOrNull(body, "idToken");
        String refreshToken = getStringOrNull(body, "refreshToken");
        String localId = getStringOrNull(body, "localId");
        String email = getStringOrNull(body, "email");
        long expiresIn = parseExpiresIn(getStringOrNull(body, "expiresIn"));

        return new FirebaseSessionTokens(accessToken, refreshToken, expiresIn, localId, email);
    }

    private String getStringOrNull(JsonObject body, String fieldName) {
        if (body.has(fieldName) && !body.get(fieldName).isJsonNull()) {
            return body.get(fieldName).getAsString();
        }
        return null;
    }

    private long parseExpiresIn(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
