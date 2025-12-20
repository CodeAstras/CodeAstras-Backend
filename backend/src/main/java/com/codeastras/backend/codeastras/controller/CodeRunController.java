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

import static com.codeastras.backend.codeastras.service.FileService.ENTRY_FILE;

@Controller
public class CodeRunController {

    private static final Logger log = LoggerFactory.getLogger(CodeRunController.class);

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

        log.info("🚀 RUN project={} user={}", projectId, userId);

        // 1️⃣ Ensure active session
        SessionRegistry.SessionInfo sessionInfo = sessionRegistry.getByProject(projectId);
        if (sessionInfo == null) {
            broadcast(projectId, new RunCodeBroadcastMessage(
                    null,
                    "No active session. Start a session first.",
                    -1,
                    userId.toString()
            ));
            return;
        }

        // 2️⃣ Decide entry file (SINGLE SOURCE OF TRUTH)
        String filename =
                (msg.getFilename() == null || msg.getFilename().isBlank())
                        ? ENTRY_FILE
                        : msg.getFilename();

        try {
            // 3️⃣ Execute code inside running container
            CommandResult result = runCodeService.runPythonInSession(
                    sessionInfo.getSessionId(),
                    filename,
                    msg.getTimeoutSeconds(),
                    userId
            );

            // 4️⃣ Broadcast output (SESSION-SCOPED)
            broadcast(projectId, new RunCodeBroadcastMessage(
                    sessionInfo.getSessionId(),
                    result.getOutput(),
                    result.getExitCode(),
                    userId.toString()
            ));

        } catch (Exception ex) {
            log.error("❌ Run failed", ex);

            broadcast(projectId, new RunCodeBroadcastMessage(
                    sessionInfo.getSessionId(),
                    "Execution failed. Check your code.",
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
