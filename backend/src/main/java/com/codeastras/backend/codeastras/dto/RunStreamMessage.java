package com.codeastras.backend.codeastras.dto;

public class RunStreamMessage {
    private String chunk;
    private boolean isFinal;
    private Integer exitCode;
    private String triggeredBy;

    public RunStreamMessage() {}

    public RunStreamMessage(String chunk, boolean isFinal, Integer exitCode, String triggeredBy) {
        this.chunk = chunk;
        this.isFinal = isFinal;
        this.exitCode = exitCode;
        this.triggeredBy = triggeredBy;
    }

    public String getChunk() { return chunk; }
    public void setChunk(String chunk) { this.chunk = chunk; }

    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean aFinal) { isFinal = aFinal; }

    public Integer getExitCode() { return exitCode; }
    public void setExitCode(Integer exitCode) { this.exitCode = exitCode; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }

}
