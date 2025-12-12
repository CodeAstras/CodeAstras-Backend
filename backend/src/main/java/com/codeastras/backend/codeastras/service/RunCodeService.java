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
import java.util.Optional;
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
     * Run python <filename> in the container associated with sessionId.
     *
     * Now validates that the caller (userId) is an accepted member of the project which owns the session.
     */
    public CommandResult runPythonInSession(String sessionId, String filename, int timeoutSeconds, UUID userId) throws Exception {
        // validate session exists
        var sessionInfoOpt = sessionRegistry.get(sessionId);
        if (sessionInfoOpt.isEmpty()) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }
        SessionRegistry.SessionInfo sessionInfo = sessionInfoOpt.get();

        UUID projectId = sessionInfo.projectId;

        // permission check: user must be owner or accepted collaborator for the project
        boolean allowed = collaboratorRepo.existsByProjectIdAndUserIdAndStatus(projectId, userId, CollaboratorStatus.ACCEPTED);

        if (!allowed) {
            throw new ForbiddenException("You are not authorized to run code in this project's session");
        }

        // derive container name
        String containerName = "session_" + sessionId;

        String safeFilename = sanitizeFilename(filename);

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("exec");
        cmd.add(containerName);
        cmd.add("python");
        cmd.add(safeFilename);

        log.info("Executing in {}: python {}", containerName, safeFilename);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            // read all available output
            String line;
            while (reader.ready() && (line = reader.readLine()) != null) {
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

            return new CommandResult(exitCode, output.toString());
        } catch (InterruptedException ie) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw ie;
        } catch (Exception ex) {
            if (process != null) process.destroyForcibly();
            throw ex;
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

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "main.py";
        String cleaned = filename;
        if (cleaned.startsWith("/") || cleaned.startsWith("\\")) cleaned = cleaned.substring(1);
        cleaned = cleaned.replace("..", "");
        if (!cleaned.endsWith(".py")) cleaned = cleaned + ".py";
        cleaned = cleaned.replaceAll("[\\r\\n\0]", "");
        return cleaned;
    }
}
