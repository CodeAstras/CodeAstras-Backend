package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CodeEditMessage;
import com.codeastras.backend.codeastras.entity.CollaboratorStatus;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.security.JwtUtils;
import com.codeastras.backend.codeastras.service.FileService;
import com.codeastras.backend.codeastras.service.FileSyncService;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
public class CodeSyncController {

    private final Logger log = LoggerFactory.getLogger(CodeSyncController.class);

    private final FileService fileService;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtils jwtUtils;
    private final ProjectCollaboratorRepository collaboratorRepo;

    public CodeSyncController(
            FileService fileService,
            FileSyncService fileSyncService,
            SessionRegistry sessionRegistry,
            SimpMessagingTemplate messagingTemplate,
            JwtUtils jwtUtils,
            ProjectCollaboratorRepository collaboratorRepo
    ) {
        this.fileService = fileService;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
        this.jwtUtils = jwtUtils;
        this.collaboratorRepo = collaboratorRepo;
    }

    @MessageMapping("/project/{projectId}/edit")
    public void handleEdit(
            @DestinationVariable UUID projectId,
            CodeEditMessage msg
    ) {
        String token = msg.getToken();
        if (token == null || token.isBlank() || !jwtUtils.validate(token)) {
            publishProjectError(projectId, "invalid_token", "Missing or invalid WS token");
            return;
        }

        UUID userId;
        try {
            Object uid = jwtUtils.getUserId(token);
            if (uid instanceof UUID) {
                userId = (UUID) uid;
            } else {
                userId = UUID.fromString(String.valueOf(uid));
            }
        } catch (Exception e) {
            log.warn("Failed to parse userId from token: {}", e.getMessage());
            publishProjectError(projectId, "invalid_token", "Failed to parse user id");
            return;
        }

        // FAST-PATH: reject if user is neither owner nor accepted collaborator
        boolean isAllowed = collaboratorRepo.existsByProjectIdAndUserIdAndStatus(projectId, userId, CollaboratorStatus.ACCEPTED);
        // also check owner by looking at collaborator table where role==OWNER and status==ACCEPTED
        // (we register owner as accepted collaborator at project creation)
        if (!isAllowed) {
            publishProjectError(projectId, "forbidden", "You are not authorized to edit this project");
            return;
        }

        String path = msg.getPath();
        String content = msg.getContent();

        if (path == null || path.isBlank()) {
            publishProjectError(projectId, "invalid_path", "Path cannot be empty");
            return;
        }
        if (content != null && content.length() > 300_000) {
            publishProjectError(projectId, "payload_too_large", "Content too large");
            return;
        }

        try {
            // 1) persist to DB (FileService will also enforce access)
            fileService.saveFileContent(projectId, path, content, userId);

            // 2) sync to container session
            var sessionInfo = sessionRegistry.getByProject(projectId);
            if (sessionInfo != null) {
                try {
                    fileSyncService.writeFileToSession(sessionInfo.sessionId, path, content);
                } catch (Exception e) {
                    log.error("Failed to sync file to session {} for project {}: {}", sessionInfo.sessionId, projectId, e.getMessage());
                    publishProjectError(projectId, "sync_failed", "Failed to sync file to running session");
                }
            }

            // 3) broadcast edit to all clients (strip token)
            CodeEditMessage out = new CodeEditMessage(
                    msg.getProjectId(),
                    msg.getUserId(),
                    path,
                    content,
                    null
            );

            messagingTemplate.convertAndSend("/topic/project/" + projectId + "/code", out);

        } catch (ForbiddenException fe) {
            publishProjectError(projectId, "forbidden", fe.getMessage());
        } catch (Exception ex) {
            log.error("Unhandled error in handleEdit: ", ex);
            publishProjectError(projectId, "internal_error", "Server error while saving file");
        }
    }

    private void publishProjectError(UUID projectId, String code, String message) {
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId + "/errors",
                (Object) Map.of("error", code, "message", message)
        );
    }
}
