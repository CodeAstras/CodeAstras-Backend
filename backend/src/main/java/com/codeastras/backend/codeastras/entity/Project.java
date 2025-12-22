package com.codeastras.backend.codeastras.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public Project() {}

    public Project(UUID id, String name, String language) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}
