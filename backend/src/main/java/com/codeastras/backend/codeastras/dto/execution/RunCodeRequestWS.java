package com.codeastras.backend.codeastras.dto.execution;

public class RunCodeRequestWS {
    private String projectId;
    private String userId;
    private String filename;
    private int timeoutSeconds = 10;
    private String token;

    public RunCodeRequestWS() {}

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}