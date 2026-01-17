package com.codeastras.backend.codeastras.service.session;

import com.codeastras.backend.codeastras.entity.project.Project;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.project.ProjectRepository;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
import com.codeastras.backend.codeastras.service.file.FileSyncService;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private static final Logger LOG =
            LoggerFactory.getLogger(SessionService.class);

    private static final int DOCKER_TIMEOUT_SEC = 120;

    private final ProjectRepository projectRepo;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;
    private final ProjectAccessManager accessManager;

    @Value("${code.runner.base-path}")
    private String sessionBasePath;

    @Value("${code.session.image-name:codeastras-collab}")
    private String sessionImage;

    public SessionService(
            ProjectRepository projectRepo,
            FileSyncService fileSyncService,
            SessionRegistry sessionRegistry,
            ProjectAccessManager accessManager
    ) {
        this.projectRepo = projectRepo;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
        this.accessManager = accessManager;
    }

    // START SESSION (IDEMPOTENT)
    public String startSession(UUID projectId, UUID userId) throws Exception {

        accessManager.require(projectId, userId, ProjectPermission.START_SESSION);

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Optional<String> existing =
                sessionRegistry.getSessionIdForProject(projectId);
        if (existing.isPresent()) {
            return existing.get();
        }

        synchronized (this) {

            Optional<String> second =
                    sessionRegistry.getSessionIdForProject(projectId);
            if (second.isPresent()) {
                return second.get();
            }

            String sessionId = UUID.randomUUID().toString();
            String containerName = "session_" + sessionId;

            LOG.info("Starting session {} for project {}", sessionId, projectId);

            try {
                fileSyncService.syncProjectToSession(projectId, sessionId);

                String output;
                try {
                    output = runCommand(
                            DOCKER_TIMEOUT_SEC,
                            "docker", "run", "-d",
                            "--name", containerName,
                            "-v", sessionBasePath + "/" + sessionId + ":/workspace",
                            sessionImage,
                            "tail", "-f", "/dev/null"
                    );
                } catch (RuntimeException e) {
                    if (e.getMessage().contains("Unable to find image")) {
                        LOG.info("Docker image missing. Pulling {}...", sessionImage);
                        runCommand(DOCKER_TIMEOUT_SEC * 2,
                                "docker", "pull", sessionImage);
                        output = runCommand(
                                DOCKER_TIMEOUT_SEC,
                                "docker", "run", "-d",
                                "--name", containerName,
                                "-v", sessionBasePath + "/" + sessionId + ":/workspace",
                                sessionImage,
                                "tail", "-f", "/dev/null"
                        );
                    } else {
                        throw e;
                    }
                }

                LOG.info("Docker container started: {}", output);

                sessionRegistry.register(
                        projectId,
                        sessionId,
                        containerName,
                        userId
                );

                return sessionId;

            } catch (Exception e) {
                LOG.error("Session start failed for project {}", projectId, e);

                try { fileSyncService.removeSessionFolder(sessionId); } catch (Exception ignored) {}
                try { runCommand(10, "docker", "rm", "-f", containerName); } catch (Exception ignored) {}

                throw e;
            }
        }
    }

    // STOP SESSION
    public void stopSession(String sessionId, UUID requesterId) throws Exception {

        SessionRegistry.SessionInfo info =
                sessionRegistry.getBySessionId(sessionId);

        if (info == null) return;

        accessManager.require(
                info.getProjectId(),
                requesterId,
                ProjectPermission.STOP_SESSION
        );

        runCommand(DOCKER_TIMEOUT_SEC,
                "docker", "rm", "-f", info.getContainerName());

        fileSyncService.removeSessionFolder(info.getSessionId());
        sessionRegistry.remove(sessionId);
    }

    // INTERNAL
    private String runCommand(int timeoutSec, String... cmd) throws Exception {

        Process p = new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader r =
                     new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean finished = p.waitFor(timeoutSec, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new RuntimeException("Command timed out: " + String.join(" ", cmd));
        }

        if (p.exitValue() != 0) {
            throw new RuntimeException(
                    "Command failed (" + p.exitValue() + "): "
                            + String.join(" ", cmd)
                            + "\nOutput:\n" + output
            );
        }

        return output.toString().trim();
    }
}
