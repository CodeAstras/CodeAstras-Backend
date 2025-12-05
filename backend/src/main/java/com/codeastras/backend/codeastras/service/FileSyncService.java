package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class FileSyncService {

    private final Logger log = LoggerFactory.getLogger(FileSyncService.class);

    private final ProjectFileRepository fileRepo;
    private final Path basePath;

    public FileSyncService(ProjectFileRepository fileRepo,
                           @Value("${code.runner.base-path:/var/code_sessions}") String basePath) {
        this.fileRepo = fileRepo;
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    }

    // SYNC FULL PROJECT INTO A SESSION DIRECTORY
    public void syncProjectToSession(UUID projectId, String sessionId) throws IOException {
        Path sessionDir = getSessionDir(sessionId);
        log.info("Syncing project {} → session {}", projectId, sessionDir);

        Files.createDirectories(sessionDir);

        List<ProjectFile> files = fileRepo.findByProjectId(projectId);

        for (ProjectFile f : files) {

            Path resolved = resolvePathSafely(sessionDir, f.getPath());

            if ("FOLDER".equalsIgnoreCase(f.getType())) {
                Files.createDirectories(resolved);
                continue;
            }

            if (resolved.getParent() != null) {
                Files.createDirectories(resolved.getParent());
            }

            String content = f.getContent() == null ? "" : f.getContent();

            Files.writeString(
                    resolved,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        }
    }


    // WRITE A SINGLE FILE INTO THE SESSION
    public void writeFileToSession(String sessionId, String path, String content) throws IOException {
        Path sessionDir = getSessionDir(sessionId);
        Path resolved = resolvePathSafely(sessionDir, path);

        if (resolved.getParent() != null) {
            Files.createDirectories(resolved.getParent());
        }

        Files.writeString(
                resolved,
                content == null ? "" : content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        log.debug("✍ Updated file {} in session {}", resolved, sessionId);
    }

    // DELETE SESSION FOLDER
    public void removeSessionFolder(String sessionId) throws IOException {
        Path dir = getSessionDir(sessionId);

        if (!Files.exists(dir)) return;

        log.info("🗑 Removing session folder {}", dir);

        Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a)) // delete deepest/files first
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        log.warn("Could not delete {}", path);
                    }
                });
    }


    // HELPER: GET SESSION DIRECTORY
    private Path getSessionDir(String sessionId) {
        return basePath.resolve(sessionId).toAbsolutePath().normalize();
    }

    // HELPER: SAFE PATH RESOLUTION
    private Path resolvePathSafely(Path sessionDir, String userPath) {

        String cleaned = (userPath == null || userPath.isBlank()) ? "main.py" : userPath;

        // Check AFTER cleaning
        if (cleaned.contains("..") || cleaned.contains("\\")) {
            throw new IllegalArgumentException("Invalid path");
        }

        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }

        Path target = sessionDir.resolve(cleaned).normalize();

        if (!target.startsWith(sessionDir)) {
            throw new IllegalArgumentException("Invalid path traversal");
        }

        return target;
    }

}
