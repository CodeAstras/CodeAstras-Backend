package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CodeEditMessage;
import com.codeastras.backend.codeastras.service.FileService;
import com.codeastras.backend.codeastras.service.FileSyncService;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class CodeSyncController {

    private final FileService fileService;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public CodeSyncController(
            FileService fileService,
            FileSyncService fileSyncService,
            SessionRegistry sessionRegistry,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.fileService = fileService;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Client sends to:
     *   /app/project/{projectId}/edit
     *
     * Server broadcasts to:
     *   /topic/project/{projectId}/code
     */
    @MessageMapping("/project/{projectId}/edit")
    public void handleEdit(
            @DestinationVariable UUID projectId,
            CodeEditMessage msg
    ) throws Exception {

        UUID userId = UUID.fromString(msg.getUserId());
        String path = msg.getPath();
        String content = msg.getContent();

        // 1. DB update
        fileService.saveFileContent(projectId, path, content, userId);

        // 2. Sync Docker session
        SessionRegistry.SessionInfo sessionInfo = sessionRegistry.getByProject(projectId);
        if (sessionInfo != null) {
            fileSyncService.writeFileToSession(sessionInfo.sessionId, path, content);
        }

        // 3. Broadcast to all connected editors in this project
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId + "/code",
                msg
        );
    }
}
