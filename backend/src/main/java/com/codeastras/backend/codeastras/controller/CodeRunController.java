package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.dto.RunCodeBroadcastMessage;
import com.codeastras.backend.codeastras.dto.RunCodeRequestWS;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.security.JwtProperties;
import com.codeastras.backend.codeastras.security.JwtUtils;
import com.codeastras.backend.codeastras.service.RunCodeService;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class CodeRunController {

    private final RunCodeService runCodeService;
    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtils jwtUtils;

    public CodeRunController(
            RunCodeService runCodeService,
            SessionRegistry sessionRegistry,
            SimpMessagingTemplate messagingTemplate, JwtUtils jwtUtils
    ) {
        this.runCodeService = runCodeService;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Correct mapping:
     * /app/project/{projectId}/run
     * Broadcast:
     * /topic/project/{projectId}/run-output
     */
    @MessageMapping("/project/{projectId}/run")
    public void handleRun(
            @DestinationVariable UUID projectId,
            RunCodeRequestWS msg
    ) throws Exception {

        // Validate token
        if (msg.getToken() == null || !jwtUtils.validate(msg.getToken())) {
            throw new ForbiddenException("Invalid or missing WS token");
        }

        UUID userId = jwtUtils.getUserId(msg.getToken());

        if (!msg.getProjectId().equals(projectId.toString())) {
            throw new ForbiddenException("Invalid projectId in WS message");
        }

        var sessionInfo = sessionRegistry.getByProject(projectId);
        if (sessionInfo == null) {
            messagingTemplate.convertAndSend(
                    "/topic/project/" + projectId + "/run-output",
                    new RunCodeBroadcastMessage("No active session", -1, userId.toString())
            );
            return;
        }

        try {
            CommandResult result = runCodeService.runPythonInSession(
                    sessionInfo.sessionId,
                    msg.getFilename(),
                    msg.getTimeoutSeconds()
            );

            messagingTemplate.convertAndSend(
                    "/topic/project/" + projectId + "/run-output",
                    new RunCodeBroadcastMessage(result.getOutput(), result.getExitCode(), userId.toString())
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSend(
                    "/topic/project/" + projectId + "/run-output",
                    new RunCodeBroadcastMessage("Error executing run: " + e.getMessage(), -1, userId.toString())
            );
        }
    }

}
