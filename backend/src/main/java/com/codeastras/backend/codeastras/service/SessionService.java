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


    public SessionService(ProjectRepository projectRepo,
                          FileSyncService fileSyncService,
                          SessionRegistry sessionRegistry) {
        this.projectRepo = projectRepo;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
    }

    public String startSession(UUID projectId, UUID userId) throws Exception {

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only OWNER can start a session
        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only project owner can start a session");
        }

        Optional<String> existing = sessionRegistry.getSessionIdForProject(projectId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String sessionId = UUID.randomUUID().toString();
        String containerName = "session_" + sessionId;

        fileSyncService.syncProjectToSession(projectId, sessionId);

        String hostPath = toDockerPath(sessionBasePath + "/" + sessionId);

        runCommand(
                "docker", "run", "-d",
                "--name", containerName,
                "-v", hostPath + ":/workspace",
                dockerImage,
                "tail", "-f", "/dev/null"
        );


        // Store who created the session
        sessionRegistry.register(projectId, sessionId, containerName, userId);

        return sessionId;
    }

    public void stopSession(String sessionId, UUID requesterId) throws Exception {

        var infoOpt = sessionRegistry.get(sessionId);
        if (infoOpt.isEmpty()) return;

        var info = infoOpt.get();

        // Only session creator can stop session
        if (!info.getOwnerUserId().equals(requesterId)) {
            throw new ForbiddenException("You cannot stop this session");
        }

        runCommand("docker", "rm", "-f", info.getContainerName());

        fileSyncService.removeSessionFolder(info.getSessionId());

        sessionRegistry.remove(sessionId);
    }

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
            throw new RuntimeException(
                    "Command failed: " + String.join(" ", cmd) +
                            "\nOutput:\n" + output
            );
        }

        return output.toString();
    }

    private String toDockerPath(String path) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Convert "C:\Users\Name\path" → "/c/Users/Name/path"
            path = path.replace("\\", "/");
            if (path.length() >= 2 && path.charAt(1) == ':') {
                char drive = Character.toLowerCase(path.charAt(0));
                return "/" + drive + path.substring(2);
            }
        }
        return path; // Linux/Mac unchanged
    }

}
