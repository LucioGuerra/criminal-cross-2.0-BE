package org.athlium.auth.domain.model;

/**
 * Enum representing the different authentication providers supported.
 */
public enum AuthProvider {
    EMAIL("email"),
    GOOGLE("google.com"),
    FACEBOOK("facebook.com");

    private final String providerId;

    AuthProvider(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderId() {
        return providerId;
    }

    /**
     * Resolves an AuthProvider from a Firebase provider ID.
     *
     * @param providerId Firebase provider ID (e.g., "google.com", "facebook.com", "password")
     * @return The corresponding AuthProvider
     */
    public static AuthProvider fromProviderId(String providerId) {
        if (providerId == null) {
            return EMAIL;
        }
        
        return switch (providerId.toLowerCase()) {
            case "google.com" -> GOOGLE;
            case "facebook.com" -> FACEBOOK;
            case "password", "email" -> EMAIL;
            default -> EMAIL;
        };
    }
}
