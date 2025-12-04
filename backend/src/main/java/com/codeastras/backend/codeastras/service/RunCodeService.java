package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RunCodeService {

    private final Logger log = LoggerFactory.getLogger(RunCodeService.class);
    private final SessionRegistry sessionRegistry;

    @Value("${code.runner.max-output-bytes:131072}") // 128KB
    private int maxOutputBytes;

    public RunCodeService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Run python <filename> in the container associated with sessionId.
     * Uses container name "session_{sessionId}" so SessionRegistry only needs sessionId.
     */
    public CommandResult runPythonInSession(String sessionId, String filename, int timeoutSeconds) throws Exception {
        // validate session exists
        var sessionInfo = sessionRegistry.get(sessionId);
        if (sessionInfo == null) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        // derive container name (this keeps SessionRegistry simple)
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
