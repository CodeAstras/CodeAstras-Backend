package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.dto.RunCodeBroadcastMessage;
import com.codeastras.backend.codeastras.dto.RunCodeRequestWS;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
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

    public CodeRunController(
            RunCodeService runCodeService,
            SessionRegistry sessionRegistry,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.runCodeService = runCodeService;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
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

        String triggeredBy = msg.getUserId();
        String filename = msg.getFilename();

        if (!msg.getProjectId().equals(projectId.toString())) {
            throw new ForbiddenException("Invalid projectId in WS message");
        }

        // find active session for this project
        var sessionInfo = sessionRegistry.getByProject(projectId);
        if (sessionInfo == null) {
            messagingTemplate.convertAndSend(
                    "/topic/project/" + projectId + "/run-output",
                    new RunCodeBroadcastMessage("No active session", -1, triggeredBy)
            );
            return;
        }

        try {
            CommandResult result = runCodeService.runPythonInSession(
                    sessionInfo.sessionId,
                    filename,
                    msg.getTimeoutSeconds()
            );

            RunCodeBroadcastMessage outgoing =
                    new RunCodeBroadcastMessage(
                            result.getOutput(),
                            result.getExitCode(),
                            triggeredBy
                    );

            messagingTemplate.convertAndSend(
                    "/topic/project/" + projectId + "/run-output",
                    outgoing
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSend(
                    "/topic/project/" + projectId + "/run-output",
                    new RunCodeBroadcastMessage(
                            "Error executing run: " + e.getMessage(),
                            -1,
                            triggeredBy
                    )
            );
        }
    }
}
