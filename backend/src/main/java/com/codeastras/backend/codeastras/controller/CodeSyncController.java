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

import java.security.Principal;

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


    @MessageMapping("/projects/{projectId}/edit")
    public void handleEdit(
            @DestinationVariable UUID projectId,
            CodeEditMessage msg,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        log.info("✏️ CODE EDIT project={} user={}", projectId, userId);

        // FAST-PATH: reject if user is not accepted collaborator (owner included)
        boolean isAllowed = collaboratorRepo
                .existsByProjectIdAndUserIdAndStatus(
                        projectId,
                        userId,
                        CollaboratorStatus.ACCEPTED
                );

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
            // 1) Persist to DB
            fileService.saveFileContent(projectId, path, content, userId);

            // 2) Sync to running session (if any)
            var sessionInfo = sessionRegistry.getByProject(projectId);
            if (sessionInfo != null) {
                try {
                    fileSyncService.writeFileToSession(
                            sessionInfo.sessionId,
                            path,
                            content
                    );
                } catch (Exception e) {
                    log.error(
                            "Failed to sync file to session {} for project {}",
                            sessionInfo.sessionId,
                            projectId,
                            e
                    );
                    publishProjectError(projectId, "sync_failed", "Failed to sync file to running session");
                }
            }

            // 3) Broadcast edit (NO token)
            CodeEditMessage out = new CodeEditMessage(
                    projectId.toString(),
                    userId.toString(),
                    path,
                    content,
                    null
            );

            messagingTemplate.convertAndSend(
                    "/topic/projects/" + projectId + "/code",
                    out
            );

        } catch (ForbiddenException fe) {
            publishProjectError(projectId, "forbidden", fe.getMessage());
        } catch (Exception ex) {
            log.error("Unhandled error in handleEdit", ex);
            publishProjectError(projectId, "internal_error", "Server error while saving file");
        }
    }


    private void publishProjectError(UUID projectId, String code, String message) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/errors",
                (Object) Map.of("error", code, "message", message)
        );
    }
}
