package com.codeastras.backend.codeastras.dto;

public class CodeEditMessage {
    private String projectId;
    private String userId;
    private String path;
    private String content;
    private String token;

    public CodeEditMessage() {}

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}

