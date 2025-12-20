package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final ProjectRepository projectRepo;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;

    @Value("${code.runner.base-path}")
    private String sessionBasePath;

    @Value("${code.runner.image-name:py-collab-runner}")
    private String dockerImage;

    public SessionService(
            ProjectRepository projectRepo,
            FileSyncService fileSyncService,
            SessionRegistry sessionRegistry
    ) {
        this.projectRepo = projectRepo;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
    }

    public String startSession(UUID projectId, UUID userId) throws Exception {

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only owner can start session");
        }

        Optional<String> existing = sessionRegistry.getSessionIdForProject(projectId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String sessionId = UUID.randomUUID().toString();
        String containerName = "session_" + sessionId;

        fileSyncService.syncProjectToSession(projectId, sessionId);

        runCommand(
                "docker", "run", "-d",
                "--name", containerName,
                "-v", sessionBasePath + "/" + sessionId + ":/workspace",
                dockerImage,
                "tail", "-f", "/dev/null"
        );

        sessionRegistry.register(projectId, sessionId, containerName, userId);
        return sessionId;
    }

    public void stopSession(String sessionId, UUID requesterId) throws Exception {

        SessionRegistry.SessionInfo info =
                sessionRegistry.getBySessionId(sessionId);

        if (info == null) return;

        if (!info.getOwnerUserId().equals(requesterId)) {
            throw new ForbiddenException("Not session owner");
        }

        runCommand("docker", "rm", "-f", info.getContainerName());
        fileSyncService.removeSessionFolder(info.getSessionId());
        sessionRegistry.remove(sessionId);
    }

    private void runCommand(String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            while (r.readLine() != null) {}
        }
        if (p.waitFor() != 0) {
            throw new RuntimeException("Command failed: " + String.join(" ", cmd));
        }
    }
}
