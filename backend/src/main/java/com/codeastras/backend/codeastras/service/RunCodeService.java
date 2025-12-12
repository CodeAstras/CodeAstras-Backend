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
    private final ProjectCollaboratorRepository collaboratorRepo;

    @Value("${code.runner.max-output-bytes:131072}") // 128KB
    private int maxOutputBytes;

    public RunCodeService(SessionRegistry sessionRegistry,
                          ProjectCollaboratorRepository collaboratorRepo) {
        this.sessionRegistry = sessionRegistry;
        this.collaboratorRepo = collaboratorRepo;
    }

    /**
     * Sanitize a path like "src/main.py" so it is safe to execute inside the container.
     * We DO NOT modify the folder structure. We only forbid traversal or invalid characters.
     */
    private String sanitizePath(String path) {
        if (path == null || path.isBlank()) {
            return "main.py";  // fallback
        }

        // Prevent path traversal
        if (path.contains("..")) {
            return "main.py";
        }

        // Remove leading slash
        if (path.startsWith("/") || path.startsWith("\\")) {
            path = path.substring(1);
        }

        return path.replaceAll("[\\r\\n\0]", "");
    }

    /**
     * Run python <filename> inside the container bound to this session.
     */
    public CommandResult runPythonInSession(String sessionId, String filename, int timeoutSeconds, UUID userId) throws Exception {

        var sessionInfoOpt = sessionRegistry.get(sessionId);
        if (sessionInfoOpt.isEmpty()) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        SessionRegistry.SessionInfo sessionInfo = sessionInfoOpt.get();
        UUID projectId = sessionInfo.projectId;

        // Only collaborators or owners can run code
        boolean allowed = collaboratorRepo.existsByProjectIdAndUserIdAndStatus(projectId, userId, CollaboratorStatus.ACCEPTED);
        if (!allowed) {
            throw new ForbiddenException("You are not authorized to run code in this project");
        }

        String containerName = "session_" + sessionId;
        String safeFilename = sanitizePath(filename);

        log.warn("RUN REQUEST FILENAME FROM FE = '{}'", filename);
        log.info("Executing in {}: python3 {}", containerName, safeFilename);

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("exec");
        cmd.add(containerName);
        cmd.add("python3");             // run python 3 explicitly
        cmd.add(safeFilename);          // run src/main.py, not main.py

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            // FIXED: proper output reading loop
            String line;
            while ((line = reader.readLine()) != null) {
                appendWithLimit(output, line + "\n");
            }

            int exitCode;
            if (!finished) {
                process.destroyForcibly();
                appendWithLimit(output, "\n[Process killed after timeout " + timeoutSeconds + "s]\n");
                exitCode = -1;
            } else {
                exitCode = process.exitValue();
            }

            // 🔥 IMPORTANT DIAGNOSTIC LOGS
            log.warn("PYTHON OUTPUT = {}", output.toString());
            log.warn("EXIT CODE = {}", exitCode);

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
