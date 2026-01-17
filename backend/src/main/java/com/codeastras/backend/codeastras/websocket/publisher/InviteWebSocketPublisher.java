package com.codeastras.backend.codeastras.websocket.publisher;

import com.codeastras.backend.codeastras.dto.collaborator.InviteNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InviteWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send invite-related notification to a single user
     */
    public void notifyUser(UUID userId, InviteNotification payload) {
        messagingTemplate.convertAndSend(
                "/topic/user/" + userId,
                payload
        );
    }
}
