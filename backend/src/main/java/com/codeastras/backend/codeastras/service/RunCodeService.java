package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.entity.CollaboratorStatus;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RunCodeService {

    private final Logger log = LoggerFactory.getLogger(RunCodeService.class);

    private final SessionRegistry sessionRegistry;
    private final PermissionService permissionService;

    @Value("${code.runner.max-output-bytes:131072}") // 128KB
    private int maxOutputBytes;

    public RunCodeService(
            SessionRegistry sessionRegistry,
            PermissionService permissionService
    ) {
        this.sessionRegistry = sessionRegistry;
        this.permissionService = permissionService;
    }

    private String sanitizePath(String path) {
        if (path == null || path.isBlank()) {
            return "main.py";
        }
        if (path.contains("..")) {
            return "main.py";
        }
        if (path.startsWith("/") || path.startsWith("\\")) {
            path = path.substring(1);
        }
        return path.replaceAll("[\\r\\n\0]", "");
    }

    public CommandResult runPythonInSession(
            String sessionId,
            String filename,
            int timeoutSeconds,
            UUID userId
    ) throws Exception {

        var sessionInfoOpt = sessionRegistry.get(sessionId);
        if (sessionInfoOpt.isEmpty()) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        SessionRegistry.SessionInfo sessionInfo = sessionInfoOpt.get();
        UUID projectId = sessionInfo.projectId;

        // 🔐 SINGLE SOURCE OF TRUTH
        permissionService.checkProjectWriteAccess(projectId, userId);

        String containerName = "session_" + sessionId;
        String safeFilename = sanitizePath(filename);

        log.info("Executing in {}: python3 {}", containerName, safeFilename);

        List<String> cmd = List.of(
                "docker",
                "exec",
                containerName,
                "python3",
                safeFilename
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            String line;
            while ((line = reader.readLine()) != null) {
                appendWithLimit(output, line + "\n");
            }

            int exitCode;
            if (!finished) {
                process.destroyForcibly();
                appendWithLimit(output,
                        "\n[Process killed after timeout " + timeoutSeconds + "s]\n");
                exitCode = -1;
            } else {
                exitCode = process.exitValue();
            }

            log.info("Run finished: exitCode={}", exitCode);
            return new CommandResult(exitCode, output.toString());
        }
    }

    private void appendWithLimit(StringBuilder sb, String s) {
        if (sb.length() + s.length() > maxOutputBytes) {
            int available = Math.max(0, maxOutputBytes - sb.length());
            if (available > 0) {
                sb.append(s, 0, available);
            }
            sb.append("\n[Output truncated]\n");
        } else {
            sb.append(s);
        }
    }
}
