package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.dto.RunCodeBroadcastMessage;
import com.codeastras.backend.codeastras.dto.RunCodeRequestWS;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.security.JwtUtils;
import com.codeastras.backend.codeastras.service.RunCodeService;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.UUID;

@Controller
public class CodeRunController {

    private final Logger log = LoggerFactory.getLogger(CodeRunController.class);

    private final RunCodeService runCodeService;
    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtils jwtUtils;

    public CodeRunController(
            RunCodeService runCodeService,
            SessionRegistry sessionRegistry,
            SimpMessagingTemplate messagingTemplate,
            JwtUtils jwtUtils
    ) {
        this.runCodeService = runCodeService;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
        this.jwtUtils = jwtUtils;
    }

    @MessageMapping("/project/{projectId}/run")
    public void handleRun(
            @DestinationVariable UUID projectId,
            RunCodeRequestWS msg
    ) {

        // -------------------------------
        // 1. Token Validation
        // -------------------------------
        if (msg.getToken() == null || !jwtUtils.validate(msg.getToken())) {
            throw new ForbiddenException("Invalid or missing WebSocket token");
        }

        UUID userId = jwtUtils.getUserId(msg.getToken());

        // -------------------------------
        // 2. Strong anti-spoofing checks
        // -------------------------------
        if (msg.getProjectId() == null ||
                !msg.getProjectId().trim().equalsIgnoreCase(projectId.toString())) {
            log.warn("WS spoof attempt detected for project {} by {}", projectId, userId);
            throw new ForbiddenException("Invalid projectId in WS message");
        }

        // -------------------------------
        // 3. Ensure there is an active session
        // -------------------------------
        SessionRegistry.SessionInfo sessionInfo = sessionRegistry.getByProject(projectId);
        if (sessionInfo == null) {
            broadcast(projectId,
                    new RunCodeBroadcastMessage("No active session found", -1, userId.toString()));
            return;
        }

        // -------------------------------
        // 4. Normalize filename
        // -------------------------------
        String filename = msg.getFilename();
        if (filename == null || filename.isBlank()) {
            filename = "main.py";
        }

        // -------------------------------
        // 5. Execute safely
        // -------------------------------
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
            log.error("Run execution failed: {}", ex.getMessage());
            broadcast(projectId, new RunCodeBroadcastMessage(
                    "Execution failed. Check your code and try again.",
                    -1,
                    userId.toString()
            ));
        }
    }

    private void broadcast(UUID projectId, RunCodeBroadcastMessage payload) {
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId + "/run-output",
                payload
        );
    }
}
