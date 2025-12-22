package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RunCodeService {

    private static final Logger log =
            LoggerFactory.getLogger(RunCodeService.class);

    private final SessionRegistry sessionRegistry;
    private final ProjectAccessManager accessManager;
    private final FileSyncService fileSyncService;
    private final ProjectFileRepository fileRepo;
    private final RunRateLimiter runRateLimiter;
    private final ExecutionLockService executionLockService;
    private final ExecutionCoordinator executionCoordinator;

    @Value("${code.runner.max-output-bytes:131072}")
    private int maxOutputBytes;

    @Value("${code.runner.python-image:codeastras-python-runner}")
    private String pythonRunnerImage;

    public CommandResult runPythonInSession(
            String sessionId,
            String filename,
            int timeoutSeconds,
            UUID userId,
            RunOutputSink sink
    ) throws Exception {

        SessionRegistry.SessionInfo session =
                sessionRegistry.getBySessionId(sessionId);

        if (session == null) {
            throw new ResourceNotFoundException("Session not found");
        }

        UUID projectId = session.getProjectId();

        if (!runRateLimiter.allow(projectId)) {
            return new CommandResult(-1, "Run limit exceeded");
        }

        if (!executionLockService.tryLock(projectId)) {
            return new CommandResult(-1, "Another execution is running");
        }

        try {
            accessManager.require(projectId, userId, ProjectPermission.EXECUTE_CODE);

            final String safePath =
                    fileSyncService.sanitizeUserPath(filename);

            // 🔒 Ensure file exists & is runnable
            ProjectFile file =
                    fileRepo.findByProjectIdAndPath(projectId, safePath);

            if (file == null || !"FILE".equalsIgnoreCase(file.getType())) {
                throw new ResourceNotFoundException(
                        "Runnable file not found: " + safePath
                );
            }

            Path jobDir = Files.createTempDirectory("codeastras-run-");
            log.info("🧪 Execution jobDir = {}", jobDir.toAbsolutePath());

            try {
                // 1️⃣ Flush editor → DB → FS
                executionCoordinator.flushBeforeExecution(projectId);

                // 2️⃣ Snapshot DB → execution FS
                fileSyncService.writeProjectSnapshot(projectId, jobDir);

                Path targetFile = jobDir.resolve(safePath);
                if (!Files.exists(targetFile)) {
                    throw new ResourceNotFoundException(
                            "File missing in snapshot: " + safePath
                    );
                }

                // 3️⃣ Runner bootstrap
                Path runner = jobDir.resolve("__runner__.py");
                Files.writeString(
                        runner,
                        """
                        import runpy, sys
                        print('[runner] running:', sys.argv[1])
                        runpy.run_path(sys.argv[1], run_name='__main__')
                        """,
                        StandardCharsets.UTF_8
                );

                // 4️⃣ Docker execution
                List<String> cmd = List.of(
                        "docker", "run", "--rm",
                        "--cpus=0.5",
                        "--memory=256m",
                        "--network=none",
                        "-w", "/workspace",
                        "-v", jobDir.toAbsolutePath() + ":/workspace",
                        pythonRunnerImage,
                        "__runner__.py",
                        safePath
                );

                log.info("🐳 Docker command:\n{}", String.join(" ", cmd));

                Process process = new ProcessBuilder(cmd)
                        .redirectErrorStream(true)
                        .start();

                StringBuilder output = new StringBuilder();

                Thread reader = new Thread(
                        () -> streamOutput(process, output, sink),
                        "OutputReader-" + projectId
                );
                reader.start();

                boolean finished =
                        process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

                if (!finished) {
                    process.destroyForcibly();
                    reader.join();
                    appendTruncatedNotice(output);
                    return new CommandResult(-1, output + "\n[Process killed]");
                }

                reader.join();
                return new CommandResult(process.exitValue(), output.toString());

            } finally {
                safeDeleteDirectory(jobDir);
            }

        } finally {
            executionLockService.unlock(projectId);
        }
    }

    private void streamOutput(
            Process process,
            StringBuilder output,
            RunOutputSink sink
    ) {
        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     process.getInputStream(),
                                     StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() + line.length() > maxOutputBytes) {
                    appendTruncatedNotice(output);
                    break;
                }
                output.append(line).append("\n");
                sink.onOutput(line);
            }
        } catch (Exception e) {
            log.error("Error streaming output", e);
        }
    }

    private void appendTruncatedNotice(StringBuilder sb) {
        if (!sb.toString().contains("[Output truncated]")) {
            sb.append("\n[Output truncated]\n");
        }
    }

    private void safeDeleteDirectory(Path dir) {
        if (dir == null || !Files.exists(dir)) return;

        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            log.warn("Failed to delete {}", p);
                        }
                    });
        } catch (IOException e) {
            log.warn("Cleanup failed for {}", dir, e);
        }
    }
}
