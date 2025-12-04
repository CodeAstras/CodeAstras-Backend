package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.dto.RunCodeBroadcastMessage;
import com.codeastras.backend.codeastras.dto.RunCodeRequestWS;
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
     * WS mapping: /app/room/{roomId}/run
     * Broadcasts final result to /topic/room/{roomId}/run-output
     */
    @MessageMapping("/room/{roomId}/run")
    public void handleRun(
            @DestinationVariable String roomId,
            RunCodeRequestWS msg
    ) throws Exception {

        // basic validation
        if (msg.getProjectId() == null || msg.getProjectId().isBlank()) {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/run-output",
                    new RunCodeBroadcastMessage("Invalid projectId", -1, msg.getUserId()));
            return;
        }

        UUID projectId = UUID.fromString(msg.getProjectId());
        String filename = msg.getFilename();
        String triggeredBy = msg.getUserId();

        // find active session for this project
        var sessionInfo = sessionRegistry.getByProject(projectId);
        if (sessionInfo == null) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/run-output",
                    new RunCodeBroadcastMessage("No active session", -1, triggeredBy)
            );
            return;
        }

        try {
            // run synchronously — this will block this handler until command returns
            // but STOMP message dispatch will continue; for heavy loads consider offloading to executor
            CommandResult result = runCodeService.runPythonInSession(sessionInfo.sessionId, filename, msg.getTimeoutSeconds());

            RunCodeBroadcastMessage outgoing =
                    new RunCodeBroadcastMessage(result.getOutput(), result.getExitCode(), triggeredBy);

            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/run-output", outgoing);
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/run-output",
                    new RunCodeBroadcastMessage("Error executing run: " + e.getMessage(), -1, triggeredBy));
        }
    }
}
