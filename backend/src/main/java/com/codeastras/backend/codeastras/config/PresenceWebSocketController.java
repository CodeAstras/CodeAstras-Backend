package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.dto.PresenceEvent;
import com.codeastras.backend.codeastras.dto.PresenceEventType;
import com.codeastras.backend.codeastras.presence.PresenceManager;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

public class PresenceWebSocketController {

    private final PresenceManager presenceManager;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceWebSocketController(PresenceManager presenceManager, SimpMessagingTemplate messagingTemplate) {
        this.presenceManager = presenceManager;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/projects/{projectId}/presence/join")
    public void join(UUID projectId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());

        presenceManager.userJoined(projectId, userId);

        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/presence",
                new PresenceEvent(
                        PresenceEventType.USER_JOINED,
                        userId,
                        projectId,
                        null,
                        Instant.now()
                )
        );
    }


    @MessageMapping("/projects/{projectId}/presence/leave")
    public void leave(UUID projectId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());

        presenceManager.userLeft(projectId, userId);

        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/presence",
                new PresenceEvent(
                        PresenceEventType.USER_LEFT,
                        userId,
                        projectId,
                        null,
                        Instant.now()
                )
        );
    }

    @MessageMapping("/projects/{projectId}/presence/file")
    public void changeFile(UUID projectId, UUID fileId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());

        presenceManager.updateFile(projectId, userId, fileId);

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/presence",
                new PresenceEvent(
                        PresenceEventType.FILE_CHANGED,
                        userId,
                        projectId,
                        fileId,
                        Instant.now()
                )
        );
    }




}
