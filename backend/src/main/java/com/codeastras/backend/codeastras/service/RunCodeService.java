package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RunCodeService {

    private final Logger log = LoggerFactory.getLogger(RunCodeService.class);

    private final SessionRegistry sessionRegistry;
    private final PermissionService permissionService;
    private final FileSyncService fileSyncService;
    private final ProjectFileRepository fileRepo;

    @Value("${code.runner.max-output-bytes:131072}")
    private int maxOutputBytes;

    public RunCodeService(
            SessionRegistry sessionRegistry,
            PermissionService permissionService,
            FileSyncService fileSyncService,
            ProjectFileRepository fileRepo
    ) {
        this.sessionRegistry = sessionRegistry;
        this.permissionService = permissionService;
        this.fileSyncService = fileSyncService;
        this.fileRepo = fileRepo;
    }

    private String sanitizePath(String path) {
        if (path == null || path.isBlank() || path.contains("..") || path.contains("\\") || path.contains("\0")) {
            return "src/main.py";
        }
        return path.replace("\r", "").replace("\n", "");
    }

    public CommandResult runPythonInSession(
            String sessionId,
            String filename,
            int timeoutSeconds,
            UUID userId
    ) throws Exception {

        SessionRegistry.SessionInfo sessionInfo =
                sessionRegistry.getBySessionId(sessionId);

        if (sessionInfo == null) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        UUID projectId = sessionInfo.getProjectId();
        permissionService.checkProjectWriteAccess(projectId, userId);

        String safePath = sanitizePath(filename);

        // 1️⃣ DB IS SOURCE OF TRUTH
        ProjectFile file = fileRepo.findByProjectIdAndPath(projectId, safePath);
        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + safePath);
        }

        String content = file.getContent() == null ? "" : file.getContent();

        // 2️⃣ FORCE SYNC INTO SESSION FS
        fileSyncService.writeFileToSession(
                sessionInfo.getSessionId(),
                safePath,
                content
        );

        String container = sessionInfo.getContainerName();
        log.info("Executing python3 {} in {}", safePath, container);

        List<String> cmd = List.of(
                "docker", "exec", container,
                "sh", "-c", "cd /workspace && python3 " + safePath
        );

        Process process = new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();

        Thread reader = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    append(output, line + "\n");
                }
            } catch (Exception ignored) {}
        });

        reader.start();

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            append(output, "\n[Process killed after timeout]\n");
            return new CommandResult(-1, output.toString());
        }

        reader.join();
        return new CommandResult(process.exitValue(), output.toString());
    }

    private void append(StringBuilder sb, String s) {
        if (sb.length() + s.length() > maxOutputBytes) {
            sb.append("\n[Output truncated]\n");
        } else {
            sb.append(s);
        }
    }
}
