package com.codeastras.backend.codeastras.service.presence;

import com.codeastras.backend.codeastras.dto.presence.PresenceEvent;
import com.codeastras.backend.codeastras.dto.presence.PresenceEventType;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
import com.codeastras.backend.codeastras.service.session.SessionFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceService {

        private final SessionFacade sessionFacade;
        private final SimpMessagingTemplate messagingTemplate;
        private final ProjectAccessManager accessManager;
        private final ApplicationEventPublisher eventPublisher;

        /**
         * Explicit user intent (join project workspace)
         * Called on WS connect / project open
         */
        public void join(UUID projectId, UUID userId) {

                accessManager.require(projectId, userId, ProjectPermission.READ_PROJECT);

                boolean joined = sessionFacade.userJoined(projectId, userId);
                if (!joined)
                        return; // idempotent

                broadcast(
                                projectId,
                                PresenceEventType.USER_JOINED,
                                userId,
                                null);
        }

        /**
         * Explicit UI-driven leave
         * (WS disconnects handled elsewhere)
         */
        public void leaveExplicit(UUID projectId, UUID userId) {

                accessManager.require(projectId, userId, ProjectPermission.READ_PROJECT);

                boolean left = sessionFacade.userLeft(projectId, userId);
                if (!left)
                        return; // idempotent

                broadcast(
                                projectId,
                                PresenceEventType.USER_LEFT,
                                userId,
                                null);
        }

        /**
         * User switched active file
         */
        public void changeFile(UUID projectId, UUID userId, UUID fileId) {

                accessManager.require(projectId, userId, ProjectPermission.READ_PROJECT);

                boolean changed = sessionFacade.updateFile(projectId, userId, fileId);
                if (!changed)
                        return;

                broadcast(
                                projectId,
                                PresenceEventType.FILE_CHANGED,
                                userId,
                                fileId);
        }

        /**
         * Heartbeat to keep presence alive
         */
        public void heartbeat(UUID projectId, UUID userId) {

                accessManager.require(projectId, userId, ProjectPermission.READ_PROJECT);

                // no broadcast — local liveness only
                sessionFacade.heartbeat(projectId, userId);
        }

        /**
         * Full presence sync for late joiners
         */
        public void sync(UUID projectId, UUID userId) {

                accessManager.require(projectId, userId, ProjectPermission.READ_PROJECT);

                messagingTemplate.convertAndSendToUser(
                                userId.toString(),
                                "/queue/presence",
                                sessionFacade.getPresenceSnapshot(projectId));
        }

        // INTERNAL

        private void broadcast(
                        UUID projectId,
                        PresenceEventType type,
                        UUID userId,
                        UUID fileId) {
                PresenceEvent event = new PresenceEvent(
                                type,
                                userId,
                                projectId,
                                fileId,
                                Instant.now());

                messagingTemplate.convertAndSend(
                                "/topic/projects/" + projectId + "/presence",
                                event);

                eventPublisher.publishEvent(event);
        }
}
