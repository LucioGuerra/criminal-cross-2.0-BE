package org.athlium.auth.domain.model;

/**
 * Entidad de dominio pura - sin dependencias de frameworks
 */
public class User {
    private String id;
    private String email;
    private String firebaseUid;
    private boolean active;

    public User() {}

    public User(String id, String email, String firebaseUid) {
        this.id = id;
        this.email = email;
        this.firebaseUid = firebaseUid;
        this.active = true;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}