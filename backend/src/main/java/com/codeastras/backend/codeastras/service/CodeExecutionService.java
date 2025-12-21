package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.dto.RunCodeBroadcastMessage;
import com.codeastras.backend.codeastras.dto.RunCodeRequestWS;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private final RunCodeService runCodeService;
    private final SessionFacade sessionFacade;
    private final SimpMessagingTemplate messagingTemplate;

    public void run(UUID projectId, RunCodeRequestWS msg, UUID userId) {

        SessionRegistry.SessionInfo session =
                sessionFacade.getSessionByProject(projectId);

        if (session == null) {
            send(projectId, new RunCodeBroadcastMessage(
                    null,
                    "RUN_ERROR",
                    "No active session. Start session first.",
                    -1,
                    userId.toString()
            ));
            return;
        }

        String filename =
                (msg.getFilename() == null || msg.getFilename().isBlank())
                        ? "main.py"
                        : msg.getFilename();

        // 🔥 RUN STARTED
        send(projectId, new RunCodeBroadcastMessage(
                session.getSessionId(),
                "RUN_STARTED",
                null,
                null,
                userId.toString()
        ));

        try {
            CommandResult result =
                    runCodeService.runPythonInSession(
                            session.getSessionId(),
                            filename,
                            msg.getTimeoutSeconds(),
                            userId,
                            line -> send(projectId,
                                    new RunCodeBroadcastMessage(
                                            session.getSessionId(),
                                            "RUN_OUTPUT",
                                            line,
                                            null,
                                            userId.toString()
                                    )
                            )
                    );

            // ✅ RUN FINISHED
            send(projectId, new RunCodeBroadcastMessage(
                    session.getSessionId(),
                    "RUN_FINISHED",
                    null,
                    result.getExitCode(),
                    userId.toString()
            ));

        } catch (Exception ex) {
            send(projectId, new RunCodeBroadcastMessage(
                    session.getSessionId(),
                    "RUN_ERROR",
                    "Execution failed",
                    -1,
                    userId.toString()
            ));
        }
    }

    private void send(UUID projectId, RunCodeBroadcastMessage payload) {
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/run-output",
                payload
        );
    }
}
