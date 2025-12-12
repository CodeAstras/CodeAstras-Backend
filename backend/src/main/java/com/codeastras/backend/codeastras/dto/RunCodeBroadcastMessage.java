package com.codeastras.backend.codeastras.dto;

public class RunCodeBroadcastMessage {
    private String output;
    private int exitCode;
    private String triggeredBy;

    public RunCodeBroadcastMessage(String output, int exitCode, String triggeredBy) {
        this.output = output;
        this.exitCode = exitCode;
        this.triggeredBy = triggeredBy;
    }

}
