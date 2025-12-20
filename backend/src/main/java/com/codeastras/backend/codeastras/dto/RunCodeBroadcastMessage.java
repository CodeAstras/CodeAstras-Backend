package com.codeastras.backend.codeastras.dto;

import lombok.Getter;

@Getter
public class RunCodeBroadcastMessage {

    private final String sessionId;
    private final String output;
    private final int exitCode;
    private final String triggeredBy;

    public RunCodeBroadcastMessage(
            String sessionId,
            String output,
            int exitCode,
            String triggeredBy
    ) {
        this.sessionId = sessionId;
        this.output = output;
        this.exitCode = exitCode;
        this.triggeredBy = triggeredBy;
    }
}
