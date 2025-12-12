package com.codeastras.backend.codeastras.dto;

import lombok.Getter;

@Getter
public class RunCodeBroadcastMessage {
    private final String output;
    private final int exitCode;
    private final String triggeredBy;

    public RunCodeBroadcastMessage(String output, int exitCode, String triggeredBy) {
        this.output = output;
        this.exitCode = exitCode;
        this.triggeredBy = triggeredBy;
    }
}
