package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CommandResult;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
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
            accessManager.requireWrite(projectId, userId);

            final String safePath = fileSyncService.sanitizeUserPath(filename);

            Path jobDir = Files.createTempDirectory("codeastras-run-");
            log.info("🧪 Execution jobDir = {}", jobDir.toAbsolutePath());

            try {
                // 1. Flush ALL pending edits to DB and Disk
                executionCoordinator.flushBeforeExecution(projectId);

                // 2. Write the snapshot (This now clears EntityManager to get fresh data)
                fileSyncService.writeProjectSnapshot(projectId, jobDir);

                // 3. Verify the file actually exists in the snapshot we just created
                Path targetFile = jobDir.resolve(safePath);
                if (!Files.exists(targetFile)) {
                    throw new ResourceNotFoundException("File not found in snapshot: " + safePath);
                }

                // ⏳ Windows/Docker Volume Sync Grace Period
                Thread.sleep(150);
                // -------------------------------
                // WRITE __runner__.py
                // -------------------------------
                Path runner = jobDir.resolve("__runner__.py");
                String runnerCode =
                        "import runpy\n" +
                                "import sys\n" +
                                "print('[runner] running:', sys.argv[1])\n" +
                                "runpy.run_path(sys.argv[1], run_name='__main__')\n";

                Files.writeString(runner, runnerCode, StandardCharsets.UTF_8);

                // -------------------------------
                // 🔍 LOG DIR CONTENTS
                // -------------------------------
                List<String> files =
                        Files.walk(jobDir)
                                .map(p -> jobDir.relativize(p).toString())
                                .collect(Collectors.toList());

                log.info("📂 jobDir contents:\n{}", String.join("\n", files));

                log.info("📜 __runner__.py contents:\n{}",
                        Files.readString(runner));

                // -------------------------------
                // DOCKER COMMAND
                // -------------------------------
                List<String> cmd = List.of(
                        "docker", "run", "--rm",
                        "--cpus=0.5",
                        "--memory=256m",
                        "--network=none",
                        "-w", "/workspace",
                        "-v", jobDir.toAbsolutePath() + ":/workspace",
                        "codeastras-python-runner",
                        "__runner__.py",
                        safePath // Use the sanitized path of the file the user actually clicked 'Run' on
                );


                log.info("🐳 Docker command:\n{}", String.join(" ", cmd));

                Process process = new ProcessBuilder(cmd)
                        .redirectErrorStream(true)
                        .start();

                StringBuilder output = new StringBuilder();

                Thread reader = new Thread(() ->
                        streamOutput(process, output, sink)
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
                Files.walk(jobDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> p.toFile().delete());
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
                             new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {

                if (output.length() + line.length() > maxOutputBytes) {
                    appendTruncatedNotice(output);
                    break;
                }

                output.append(line).append("\n");
                sink.onOutput(line);
            }
        } catch (Exception ignored) {}
    }

    private void appendTruncatedNotice(StringBuilder sb) {
        if (!sb.toString().contains("[Output truncated]")) {
            sb.append("\n[Output truncated]\n");
        }
    }
}
