package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CodeEditMessage;
import com.codeastras.backend.codeastras.dto.ProjectErrorMessage;
import com.codeastras.backend.codeastras.entity.CollaboratorStatus;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.service.DebouncedFileSaveManager;
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

    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProjectCollaboratorRepository collaboratorRepo;
    private final DebouncedFileSaveManager debouncedFileSaveManager;

    public CodeSyncController(
            FileSyncService fileSyncService,
            SessionRegistry sessionRegistry,
            SimpMessagingTemplate messagingTemplate,
            ProjectCollaboratorRepository collaboratorRepo,
            DebouncedFileSaveManager debouncedFileSaveManager
    ) {
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
        this.collaboratorRepo = collaboratorRepo;
        this.debouncedFileSaveManager = debouncedFileSaveManager;
    }

    @MessageMapping("/projects/{projectId}/edit")
    public void handleEdit(
            @DestinationVariable UUID projectId,
            CodeEditMessage msg,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        log.debug("✏️ CODE EDIT project={} user={}", projectId, userId);

        // 🔐 Permission check
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
            // 1️⃣ Debounced persistence (DB + project FS)
            debouncedFileSaveManager.scheduleSave(
                    projectId,
                    path,
                    content,
                    userId
            );

            // 2️⃣ Immediate sync to active session (for live execution)
            var sessionInfo = sessionRegistry.getByProject(projectId);
            if (sessionInfo != null) {
                fileSyncService.writeFileToSession(
                        sessionInfo.getSessionId(),
                        path,
                        content
                );
            }

            // 3️⃣ Broadcast edit to collaborators
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
                new ProjectErrorMessage(code, message)
        );
    }

}
