package com.codeastras.backend.codeastras.Model;

public class ProjectSession {

    private String projectId;
    private String language;
    private String createdAt;

    public ProjectSession(String projectId, String language, String createdAt) {
        this.projectId = projectId;
        this.language = language;
        this.createdAt = createdAt;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
