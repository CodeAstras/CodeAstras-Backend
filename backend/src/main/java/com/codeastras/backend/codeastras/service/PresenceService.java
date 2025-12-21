package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.PresenceEvent;
import com.codeastras.backend.codeastras.dto.PresenceEventType;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final SessionFacade sessionFacade;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Explicit user intent (join project workspace)
     * Called on WS connect / project open
     */
    public void join(UUID projectId, UUID userId) {
        sessionFacade.userJoined(projectId, userId);

        broadcast(
                projectId,
                PresenceEventType.USER_JOINED,
                userId,
                null
        );
    }

    /**
     * DO NOT call this from WebSocket disconnect.
     * Disconnects are handled via wsSessionId centrally.
     *
     * This is only for explicit UI-driven leave (optional).
     */
    public void leaveExplicit(UUID projectId, UUID userId) {
        sessionFacade.userLeft(projectId, userId);

        broadcast(
                projectId,
                PresenceEventType.USER_LEFT,
                userId,
                null
        );
    }

    /**
     * User switched active file
     */
    public void changeFile(UUID projectId, UUID userId, UUID fileId) {
        sessionFacade.updateFile(projectId, userId, fileId);

        broadcast(
                projectId,
                PresenceEventType.FILE_CHANGED,
                userId,
                fileId
        );
    }

    /**
     * Heartbeat to keep presence alive
     */
    public void heartbeat(UUID projectId, UUID userId) {
        sessionFacade.heartbeat(projectId, userId);
    }

    /**
     * Full presence sync for late joiners
     */
    public void sync(UUID projectId, UUID userId) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/presence",
                sessionFacade.getPresenceSnapshot(projectId)
        );
    }

    // ---------------- INTERNAL ----------------

    private void broadcast(
            UUID projectId,
            PresenceEventType type,
            UUID userId,
            UUID fileId
    ) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/presence",
                new PresenceEvent(
                        type,
                        userId,
                        projectId,
                        fileId,
                        Instant.now()
                )
        );
    }
}
