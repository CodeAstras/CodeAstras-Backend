package com.codeastras.backend.codeastras.entity.auth;



import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    // --------------------
    // Identity (IMMUTABLE)
    // --------------------

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // OAuth
    @Column(nullable = false)
    private String provider;   // LOCAL, GITHUB, GOOGLE

    @Column(name = "provider_id")
    private String providerId;

    // Profile (MUTABLE)
    /**
     * Public unique handle.
     * Guarded by ProfileCommandService.
     */
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    /**
     * Display name shown publicly.
     * NOT legal name.
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 160)
    private String bio;

    @Column(length = 100)
    private String location;

    @Column(name = "avatar_url")
    private String avatarUrl;

    // Constructors
    protected User() {
        // JPA only
    }

    public User(String fullName, String username, String email, String passwordHash, String provider) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.provider = provider;
        this.createdAt = Instant.now();
    }

    // Getters (NO LOGIC)

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBio() {
        return bio;
    }

    public String getLocation() {
        return location;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    // --------------------
    // Setters (RESTRICTED)
    // --------------------
    // These MUST only be called from ProfileCommandService

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    // --------------------
    // OAuth helpers
    // --------------------

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
