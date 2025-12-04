package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.service.FileService;
import com.codeastras.backend.codeastras.service.FileSyncService;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import com.codeastras.backend.codeastras.dto.CodeEditMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class CodeSyncController {

    private final FileService fileService;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;

    public CodeSyncController(FileService fileService, FileSyncService fileSyncService, SessionRegistry sessionRegistry) {
        this.fileService = fileService;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
    }
    @MessageMapping("/room/{roomId}/edit")
    @SendTo("/topic/room/{roomId}/code")
    public CodeEditMessage handleEdit(
            @DestinationVariable String roomId,
            CodeEditMessage msg
    ) throws Exception {

        UUID projectId = UUID.fromString(msg.getProjectId());
        UUID userId = UUID.fromString(msg.getUserId());
        String path = msg.getPath();
        String content = msg.getContent();

        // 1. Save updated content to database
        fileService.saveFileContent(projectId, path, content, userId);

        // 2. Sync to active Docker session
        SessionRegistry.SessionInfo sessionInfo = sessionRegistry.getByProject(projectId);
        if (sessionInfo != null) {
            fileSyncService.writeFileToSession(sessionInfo.sessionId, path, content);
        }

        // 3. Broadcast to all subscribers in the room
        return msg;
    }


    @MessageMapping("/code/{projectId}")
    @SendTo("/topic/code/{projectId}")
    public String syncCode(@DestinationVariable String projectId, String Code) {
        return Code;
    }
}

