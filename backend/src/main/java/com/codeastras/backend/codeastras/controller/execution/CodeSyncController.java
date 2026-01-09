package com.codeastras.backend.codeastras.controller.execution;

import com.codeastras.backend.codeastras.dto.execution.CodeEditMessage;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.execution.CodeEditingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
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
