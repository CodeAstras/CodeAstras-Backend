package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CodeEditMessage;
import com.codeastras.backend.codeastras.dto.ProjectErrorMessage;
import com.codeastras.backend.codeastras.entity.CollaboratorStatus;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CodeEditingService {

    private final ProjectCollaboratorRepository collaboratorRepo;
    private final DebouncedFileSaveManager debouncedFileSaveManager;
    private final SessionFacade sessionFacade;
    private final FileSyncService fileSyncService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int MAX_CONTENT_SIZE = 300_000;

    public void handleEdit(UUID projectId, CodeEditMessage msg, UUID userId) {

        boolean isAllowed = collaboratorRepo
                .existsByProjectIdAndUserIdAndStatus(
                        projectId,
                        userId,
                        CollaboratorStatus.ACCEPTED
                );

        if (!isAllowed) {
            publishError(projectId, "forbidden", "Not authorized");
            return;
        }

        final String path;
        try {
            path = fileSyncService.sanitizeUserPath(msg.getPath());
        } catch (IllegalArgumentException e) {
            publishError(projectId, "invalid_path", e.getMessage());
            return;
        }

        String content = msg.getContent();

        if (content != null && content.length() > MAX_CONTENT_SIZE) {
            publishError(projectId, "payload_too_large", "Content too large");
            return;
        }

        debouncedFileSaveManager.scheduleSave(
                projectId,
                path,
                content,
                userId
        );

        SessionRegistry.SessionInfo session =
                sessionFacade.getSessionByProject(projectId);

        if (session != null) {
            try {
                fileSyncService.writeFileToSession(
                        session.getSessionId(),
                        path,
                        content
                );
            } catch (Exception ignored) {
            }
        }

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/code",
                new CodeEditMessage(
                        projectId.toString(),
                        userId.toString(),
                        path,
                        content,
                        null
                )
        );
    }

    private void publishError(UUID projectId, String code, String message) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/errors",
                new ProjectErrorMessage(code, message)
        );
    }
}
