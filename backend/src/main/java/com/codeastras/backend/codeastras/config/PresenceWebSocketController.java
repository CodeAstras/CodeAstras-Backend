package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.dto.PresenceEvent;
import com.codeastras.backend.codeastras.dto.PresenceEventType;
import com.codeastras.backend.codeastras.presence.PresenceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PresenceWebSocketController {

    private final PresenceManager presenceManager;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/projects/{projectId}/presence/join")
    public void join(
            @DestinationVariable UUID projectId,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        presenceManager.userJoined(projectId, userId);

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/presence",
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
    public void leave(
            @DestinationVariable UUID projectId,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        presenceManager.userLeft(projectId, userId);

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/presence",
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
    public void changeFile(
            @DestinationVariable UUID projectId,
            UUID fileId,
            Principal principal
    ) {
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

    @MessageMapping("/projects/{projectId}/presence/sync")
    public void syncPresence(
            @DestinationVariable UUID projectId,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/presence",
                presenceManager.getPresenceSnapshot(projectId)
        );
    }

}
