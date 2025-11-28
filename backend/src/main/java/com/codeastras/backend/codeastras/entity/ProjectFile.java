package com.codeastras.backend.codeastras.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_files")
public class ProjectFile {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String path;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String type; // FILE or FOLDER

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProjectFile() {}

    public ProjectFile(UUID id, UUID projectId, String path, String content, String type) {
        this.id = id;
        this.projectId = projectId;
        this.path = path;
        this.content = content;
        this.type = type;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
