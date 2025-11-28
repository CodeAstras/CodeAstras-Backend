package com.codeastras.backend.codeastras.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

public class CodeSyncController {

    @MessageMapping("/code/{projectId}")
    @SendTo("/topic/code/{projectId}")
    public String syncCode(@DestinationVariable String projectId, String Code) {
        return Code;
    }
}

