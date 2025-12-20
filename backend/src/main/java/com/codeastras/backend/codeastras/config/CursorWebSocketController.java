package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.dto.CursorUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CursorWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/projects/{projectId}/cursor")
    public void handleCursorUpdate(
            @DestinationVariable UUID projectId,
            CursorUpdateMessage message,
            Principal principal
    ) {
        UUID senderId = UUID.fromString(principal.getName());

        if (!senderId.equals(message.getUserId())) {
            throw new IllegalStateException("Invalid cursor update");
        }

        String topic =
                "/topic/projects/" +
                        projectId +
                        "/files/" +
                        message.getFileId() +
                        "/cursor";

        messagingTemplate.convertAndSend(topic, message);
    }
}
