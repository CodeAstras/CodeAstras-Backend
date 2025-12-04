package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import com.codeastras.backend.codeastras.store.SessionRegistry.SessionInfo;
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

    @Value("${code.runner.image-name:py-collab-runner}")
    private String dockerImage;

    public SessionService(ProjectRepository projectRepo,
                          FileSyncService fileSyncService,
                          SessionRegistry sessionRegistry) {
        this.projectRepo = projectRepo;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
    }

    /** Start a coding session (one container per project) */
    public String startSession(UUID projectId, UUID userId) throws Exception {

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Check if there is already a session
        Optional<String> existing = sessionRegistry.getSessionIdForProject(projectId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new session
        String sessionId = UUID.randomUUID().toString();
        String containerName = "session_" + sessionId;

        // Sync DB files into /workspace for this session
        fileSyncService.syncProjectToSession(projectId, sessionId);

        // Start Docker container
        runCommand(
                "docker", "run", "-d",
                "--name", containerName,
                "-v", "/var/code_sessions/" + sessionId + ":/workspace",
                dockerImage,
                "tail", "-f", "/dev/null"
        );

        // Register with proper UUIDs
        SessionInfo info = new SessionInfo(
                sessionId,
                containerName,
                projectId,
                userId
        );

        sessionRegistry.register(info);

        return sessionId;
    }

    /** Stop container + remove session */
    public void stopSession(String sessionId) throws Exception {
        Optional<SessionInfo> infoOpt = sessionRegistry.get(sessionId);
        if (infoOpt.isEmpty()) return;

        SessionInfo info = infoOpt.get();

        // Stop container
        runCommand("docker", "rm", "-f", info.containerName);

        // Delete session folder
        fileSyncService.removeSessionFolder(sessionId);

        // Remove registry
        sessionRegistry.remove(sessionId);
    }

    /** Utility to run system commands */
    private String runCommand(String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder output = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exit = p.waitFor();
        if (exit != 0) {
            throw new RuntimeException("Command failed: " + String.join(" ", cmd) +
                    "\nOutput: " + output);
        }

        return output.toString();
    }
}
