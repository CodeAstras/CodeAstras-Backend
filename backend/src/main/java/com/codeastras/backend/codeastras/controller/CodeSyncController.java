package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CodeEditMessage;
import com.codeastras.backend.codeastras.dto.ProjectErrorMessage;
import com.codeastras.backend.codeastras.entity.CollaboratorStatus;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.CodeEditingService;
import com.codeastras.backend.codeastras.service.DebouncedFileSaveManager;
import com.codeastras.backend.codeastras.service.FileSyncService;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CodeSyncController {

    private final CodeEditingService codeEditingService;

    @MessageMapping("/projects/{projectId}/edit")
    public void handleEdit(
            @DestinationVariable UUID projectId,
            CodeEditMessage msg,
            Principal principal
    ) {
        UUID userId = AuthUtil.requireUserId(principal);

        codeEditingService.handleEdit(projectId, msg, userId);
    }
}
