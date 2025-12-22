package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CodeEditMessage;
import com.codeastras.backend.codeastras.dto.ProjectErrorMessage;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CodeEditingService {

    private static final int MAX_CONTENT_SIZE = 300_000;

    private final DebouncedFileSaveManager debouncedFileSaveManager;
    private final FileSyncService fileSyncService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProjectAccessManager accessManager;

    // -------------------------------------------------
    // MAIN ENTRY
    // -------------------------------------------------

    public void handleEdit(UUID projectId, CodeEditMessage msg, UUID userId) {

        // 🔐 AUTHORIZATION
        try {
            accessManager.require(
                    projectId,
                    userId,
                    ProjectPermission.UPDATE_FILE
            );
        } catch (ForbiddenException e) {
            publishError(projectId, "forbidden", "Not authorized");
            return;
        }

        // 🧼 PATH VALIDATION
        final String path;
        try {
            path = fileSyncService.sanitizeUserPath(msg.getPath());
        } catch (IllegalArgumentException e) {
            publishError(projectId, "invalid_path", e.getMessage());
            return;
        }

        // 📦 PAYLOAD VALIDATION
        String content = msg.getContent();
        if (content != null && content.length() > MAX_CONTENT_SIZE) {
            publishError(projectId, "payload_too_large", "Content too large");
            return;
        }

        // 🕒 ASYNC DURABLE SAVE (DB + FS + SESSION handled later)
        debouncedFileSaveManager.scheduleSave(
                projectId,
                path,
                content,
                userId
        );

        // 📡 REAL-TIME BROADCAST
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

    // -------------------------------------------------

    private void publishError(UUID projectId, String code, String message) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/errors",
                new ProjectErrorMessage(code, message)
        );
    }
}
