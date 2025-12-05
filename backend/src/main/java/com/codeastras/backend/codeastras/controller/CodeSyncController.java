package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CodeEditMessage;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.security.JwtUtils;
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
    private final JwtUtils jwtUtils;

    public CodeSyncController(
            FileService fileService,
            FileSyncService fileSyncService,
            SessionRegistry sessionRegistry,
            SimpMessagingTemplate messagingTemplate, JwtUtils jwtUtils
    ) {
        this.fileService = fileService;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
        this.jwtUtils = jwtUtils;
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

        // 0. Validate token
        if (msg.getToken() == null || !jwtUtils.validate(msg.getToken())) {
            throw new ForbiddenException("Invalid or missing WS token");
        }

        UUID userId = jwtUtils.getUserId(msg.getToken());
        String path = msg.getPath();
        String content = msg.getContent();

        // 1. Save to DB
        fileService.saveFileContent(projectId, path, content, userId);

        // 2. Sync Docker session
        var sessionInfo = sessionRegistry.getByProject(projectId);
        if (sessionInfo != null) {
            fileSyncService.writeFileToSession(sessionInfo.sessionId, path, content);
        }

        // 3. Broadcast to all users
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId + "/code",
                msg
        );
    }

}
