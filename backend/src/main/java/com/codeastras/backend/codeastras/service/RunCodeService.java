package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import com.codeastras.backend.codeastras.store.SessionRegistry.SessionInfo;
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

    private static final Logger log = LoggerFactory.getLogger(RunCodeService.class);

    private final SessionRegistry sessionRegistry;

    @Value("${code.runner.max-output-bytes:65536}")
    private int maxOutputBytes;

    public RunCodeService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public CommandResult runPythonInSession(String sessionId, String filename, int timeoutSeconds) throws Exception {

        SessionInfo sessionInfo = sessionRegistry.get(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        String containerName = sessionInfo.containerName;
        String safeFilename = sanitizeFilename(filename);

        List<String> cmd = List.of(
                "docker", "exec", containerName,
                "python", safeFilename
        );

        log.info("Running code in container {}: python {}", containerName, safeFilename);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            // Drain output
            while (reader.ready()) {
                appendWithLimit(output, reader.readLine() + "\n");
            }

            if (!finished) {
                process.destroyForcibly();
                appendWithLimit(output, "[Process killed after " + timeoutSeconds + "s]\n");
                return new CommandResult(-1, output.toString());
            }

            return new CommandResult(process.exitValue(), output.toString());
        }
    }

    private void appendWithLimit(StringBuilder sb, String s) {
        if (sb.length() + s.length() > maxOutputBytes) {
            int allowed = maxOutputBytes - sb.length();
            if (allowed > 0) {
                sb.append(s, 0, allowed);
            }
            sb.append("\n[Output truncated]\n");
        } else {
            sb.append(s);
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "main.py";

        String cleaned = filename.replace("..", "").replace("\\", "");
        if (cleaned.startsWith("/")) cleaned = cleaned.substring(1);
        if (!cleaned.endsWith(".py")) cleaned += ".py";

        return cleaned;
    }
}
