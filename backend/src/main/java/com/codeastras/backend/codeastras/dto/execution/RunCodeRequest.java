package com.codeastras.backend.codeastras.dto.execution;

public class RunCodeRequest {
    private String filename;
    private int timeoutSeconds = 5; // default

    public RunCodeRequest() {}

    public RunCodeRequest(String filename, int timeoutSeconds) {
        this.filename = filename;
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
