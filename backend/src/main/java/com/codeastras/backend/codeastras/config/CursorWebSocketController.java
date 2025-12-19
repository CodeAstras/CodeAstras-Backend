package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.dto.CursorUpdateMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.UUID;

public class CursorWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public CursorWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/projects/{projectId}/cursor")
    public void handleCursorUpdate(CursorUpdateMessage message, Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());

        if (!senderId.equals(message.getUserId())) {
            throw new IllegalStateException("Invalid cursor update");
        }
        String topic = "/topic/projects/" +
                message.getProjectId() +
                "/files/" +
                message.getFileId() +
                "/cursor";

        messagingTemplate.convertAndSend(topic, message);
    }
}
