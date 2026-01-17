package com.codeastras.backend.codeastras.dto.execution;

import lombok.Getter;

@Getter
public class RunCodeBroadcastMessage {

    private final String sessionId;
    private final String type;       // RUN_STARTED | RUN_OUTPUT | RUN_FINISHED | RUN_ERROR
    private final String output;     // only for RUN_OUTPUT
    private final Integer exitCode;  // only for RUN_FINISHED
    private final String triggeredBy;

    public RunCodeBroadcastMessage(
            String sessionId,
            String type,
            String output,
            Integer exitCode,
            String triggeredBy
    ) {
        this.sessionId = sessionId;
        this.type = type;
        this.output = output;
        this.exitCode = exitCode;
        this.triggeredBy = triggeredBy;
    }
}
