package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class FileSyncService {

    private static final Logger log =
            LoggerFactory.getLogger(FileSyncService.class);

    private final ProjectFileRepository fileRepo;
    private final Path basePath;

    @PersistenceContext
    private EntityManager entityManager;

    public FileSyncService(
            ProjectFileRepository fileRepo,
            @Value("${code.runner.base-path:/var/code_sessions}") String basePath
    ) {
        this.fileRepo = fileRepo;
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create session base path", e);
        }
    }

    // ==================================================
    // CANONICAL PATH SANITIZER (SINGLE SOURCE)
    // ==================================================

    public String sanitizeUserPath(String userPath) {
        if (userPath == null || userPath.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        String cleaned = userPath
                .replace("\\", "/")
                .replaceAll("/{2,}", "/");

        if (cleaned.contains("..") || cleaned.contains("\0")) {
            throw new IllegalArgumentException("Invalid path traversal");
        }

        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }

        return cleaned;
    }

    // ==================================================
    // COLLAB SESSION SYNC
    // ==================================================

    public void syncProjectToSession(UUID projectId, String sessionId)
            throws IOException {

        Path sessionDir = getSessionDir(sessionId);
        Files.createDirectories(sessionDir);

        log.info("🔁 Syncing project {} → session {}", projectId, sessionDir);

        List<ProjectFile> files = fileRepo.findByProjectId(projectId);

        for (ProjectFile f : files) {

            String safePath = sanitizeUserPath(f.getPath());
            Path resolved = resolvePathSafely(sessionDir, safePath);

            try {
                if ("FOLDER".equalsIgnoreCase(f.getType())) {
                    Files.createDirectories(resolved);
                    continue;
                }

                Files.createDirectories(resolved.getParent());

                Files.writeString(
                        resolved,
                        f.getContent() == null ? "" : f.getContent(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException e) {
                log.error("Failed to sync file {} to session {}", safePath, sessionId, e);
            }
        }
    }

    // ==================================================
    // 🔥 LIVE FILE UPDATE
    // ==================================================

    public void writeFileToSession(
            String sessionId,
            String userPath,
            String content
    ) throws IOException {

        Path sessionDir = getSessionDir(sessionId);
        String safePath = sanitizeUserPath(userPath);
        Path resolved = resolvePathSafely(sessionDir, safePath);

        Files.createDirectories(resolved.getParent());

        Files.writeString(
                resolved,
                content == null ? "" : content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        log.debug("✍ Updated {} in session {}", safePath, sessionId);
    }

    // ==================================================
    // EXECUTION SNAPSHOT (IMMUTABLE)
    // ==================================================

    public void writeProjectSnapshot(UUID projectId, Path snapshotDir)
            throws IOException {

        Files.createDirectories(snapshotDir);

        // 🔥 Ensure no stale entities
        entityManager.clear();

        List<ProjectFile> files = fileRepo.findByProjectId(projectId);

        if (files.isEmpty()) {
            throw new IllegalStateException("Project has no files");
        }

        for (ProjectFile f : files) {

            String safePath = sanitizeUserPath(f.getPath());
            Path resolved = resolvePathSafely(snapshotDir, safePath);

            try {
                if ("FOLDER".equalsIgnoreCase(f.getType())) {
                    Files.createDirectories(resolved);
                    continue;
                }

                Files.createDirectories(resolved.getParent());

                String content = f.getContent() == null ? "" : f.getContent();

                Files.writeString(
                        resolved,
                        content,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException e) {
                log.error("Snapshot write failed for {}", safePath, e);
                throw e; // snapshot must be correct or fail hard
            }
        }
    }

    // ==================================================
    // SESSION CLEANUP
    // ==================================================

    public void removeSessionFolder(String sessionId) throws IOException {

        Path sessionDir = getSessionDir(sessionId);
        if (!Files.exists(sessionDir)) return;

        log.info("🗑 Removing session folder {}", sessionDir);

        Files.walk(sessionDir)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        log.warn("Failed to delete {}", p);
                    }
                });
    }

    // ==================================================
    // INTERNAL HELPERS
    // ==================================================

    private Path getSessionDir(String sessionId) {
        return basePath.resolve(sessionId).normalize();
    }

    private Path resolvePathSafely(Path baseDir, String safePath) {
        Path target = baseDir.resolve(safePath).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Path traversal detected");
        }
        return target;
    }
}
