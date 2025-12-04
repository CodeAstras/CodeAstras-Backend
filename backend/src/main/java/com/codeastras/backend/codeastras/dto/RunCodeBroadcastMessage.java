package com.codeastras.backend.codeastras.dto;

public class RunCodeBroadcastMessage {
    private String output;
    private int exitCode;
    private String triggeredBy;

    public RunCodeBroadcastMessage() {}

    public RunCodeBroadcastMessage(String output, int exitCode, String triggeredBy) {
        this.output = output;
        this.exitCode = exitCode;
        this.triggeredBy = triggeredBy;
    }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }

    public int getExitCode() { return exitCode; }
    public void setExitCode(int exitCode) { this.exitCode = exitCode; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
}
