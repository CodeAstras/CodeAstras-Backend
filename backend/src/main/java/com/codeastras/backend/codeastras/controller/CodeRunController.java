package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.dto.RunCodeBroadcastMessage;
import com.codeastras.backend.codeastras.dto.RunCodeRequestWS;
import com.codeastras.backend.codeastras.service.RunCodeService;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class CodeRunController {

    private final Logger log = LoggerFactory.getLogger(CodeRunController.class);

    private final RunCodeService runCodeService;
    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public CodeRunController(
            RunCodeService runCodeService,
            SessionRegistry sessionRegistry,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.runCodeService = runCodeService;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/projects/{projectId}/run")
    public void handleRun(
            @DestinationVariable UUID projectId,
            RunCodeRequestWS msg,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());

        log.info("🚀 RUN REQUEST RECEIVED project={} user={}", projectId, userId);

        // 1. Ensure active session
        SessionRegistry.SessionInfo sessionInfo = sessionRegistry.getByProject(projectId);
        if (sessionInfo == null) {
            broadcast(projectId, new RunCodeBroadcastMessage(
                    "No active session found",
                    -1,
                    userId.toString()
            ));
            return;
        }

        // 2. Normalize filename
        String filename = (msg.getFilename() == null || msg.getFilename().isBlank())
                ? "main.py"
                : msg.getFilename();

        // 3. Execute
        try {
            CommandResult result = runCodeService.runPythonInSession(
                    sessionInfo.sessionId,
                    filename,
                    msg.getTimeoutSeconds(),
                    userId
            );

            broadcast(projectId, new RunCodeBroadcastMessage(
                    result.getOutput(),
                    result.getExitCode(),
                    userId.toString()
            ));

        } catch (Exception ex) {
            log.error("Run execution failed", ex);
            broadcast(projectId, new RunCodeBroadcastMessage(
                    "Execution failed. Check your code and try again.",
                    -1,
                    userId.toString()
            ));
        }
    }

    private void broadcast(UUID projectId, RunCodeBroadcastMessage payload) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/run-output",
                payload
        );
    }
}
