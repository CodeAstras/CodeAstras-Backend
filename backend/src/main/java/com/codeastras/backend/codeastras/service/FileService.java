package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.config.StorageProperties;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Logger log =
            LoggerFactory.getLogger(FileService.class);

    public static final String ENTRY_FILE = "src/main.py";

    private final ProjectFileRepository fileRepo;
    private final ProjectAccessManager accessManager;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;
    private final StorageProperties storageProperties;

    // ==================================================
    // PATH HELPERS
    // ==================================================

    public String validateAndNormalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        if (path.startsWith("/") || path.startsWith("\\")) {
            throw new IllegalArgumentException("Path cannot start with slash");
        }
        if (path.contains("..") || path.contains("\0")) {
            throw new IllegalArgumentException("Invalid path traversal");
        }
        return path.replace("\\", "/");
    }

    private Path projectRoot(UUID projectId) {
        return Paths.get(storageProperties.getProjects())
                .resolve(projectId.toString())
                .toAbsolutePath()
                .normalize();
    }

    // ==================================================
    // READS
    // ==================================================

    public List<ProjectFile> findAll(UUID projectId, UUID userId) {
        accessManager.requireRead(projectId, userId);
        return fileRepo.findByProjectId(projectId);
    }

    public ProjectFile getFile(UUID projectId, String path, UUID userId) {
        accessManager.requireRead(projectId, userId);

        String safe = validateAndNormalizePath(path);
        ProjectFile file = fileRepo.findByProjectIdAndPath(projectId, safe);

        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + safe);
        }
        return file;
    }

    // ==================================================
    // CREATE
    // ==================================================

    @Transactional
    public ProjectFile createFile(
            UUID projectId,
            String path,
            String type,
            UUID userId
    ) throws IOException {

        accessManager.requireWrite(projectId, userId);

        String safe = validateAndNormalizePath(path);
        String t = type.toUpperCase();

        if (!t.equals("FILE") && !t.equals("FOLDER")) {
            throw new IllegalArgumentException("Invalid type");
        }

        if (fileRepo.findByProjectIdAndPath(projectId, safe) != null) {
            throw new IllegalStateException("Already exists");
        }

        ProjectFile pf = new ProjectFile(
                UUID.randomUUID(),
                projectId,
                safe,
                t.equals("FILE") ? "" : null,
                t
        );
        pf.setCreatedAt(Instant.now());
        pf.setUpdatedAt(Instant.now());
        fileRepo.save(pf);

        Path root = projectRoot(projectId);
        Path resolved = root.resolve(safe).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path");
        }

        if (t.equals("FOLDER")) {
            Files.createDirectories(resolved);
        } else {
            Files.createDirectories(resolved.getParent());
            Files.writeString(
                    resolved,
                    "",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );
        }

        // Sync to active session if present
        sessionRegistry.getSessionIdForProject(projectId)
                .ifPresent(sessionId -> {
                    try {
                        if (t.equals("FILE")) {
                            fileSyncService.writeFileToSession(sessionId, safe, "");
                        }
                    } catch (Exception e) {
                        log.warn("Session sync failed", e);
                    }
                });

        return pf;
    }

    // ==================================================
    // SAVE (NORMAL PATH — DEBOUNCED CALLS)
    // ==================================================

    @Transactional
    public ProjectFile save(
            UUID projectId,
            String path,
            String content,
            UUID userId
    ) throws IOException {

        accessManager.requireWrite(projectId, userId);

        String safe = validateAndNormalizePath(path);
        // Force fetch from DB to ensure we have the attached entity
        ProjectFile file = fileRepo.findByProjectIdAndPath(projectId, safe);

        if (file == null) {
            throw new ResourceNotFoundException("File not found");
        }

        if (!"FILE".equalsIgnoreCase(file.getType())) {
            throw new IllegalArgumentException("Not a file");
        }

        // 1. Update physical project storage
        Path resolved = projectRoot(projectId).resolve(safe).normalize();
        Files.createDirectories(resolved.getParent());
        Files.writeString(
                resolved,
                content == null ? "" : content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        // 2. Update Database
        file.setContent(content);
        file.setUpdatedAt(Instant.now());

        // Use saveAndFlush to force Hibernate to generate the UPDATE SQL immediately
        fileRepo.saveAndFlush(file);

        // 3. Update active session if present
        sessionRegistry.getSessionIdForProject(projectId)
                .ifPresent(sessionId -> {
                    try {
                        fileSyncService.writeFileToSession(sessionId, safe, content);
                    } catch (Exception e) {
                        log.warn("Session sync failed", e);
                    }
                });

        return file;
    }

    // ==================================================
    // 🔥 FORCED FLUSH (CRITICAL)
    // ==================================================

    /**
     * Forces a synchronous persistence of a single file.
     * Used BEFORE execution & snapshot creation.
     */
    @Transactional
    public void flushSingle(UUID projectId, String path) {

        String safe = validateAndNormalizePath(path);

        ProjectFile file =
                fileRepo.findByProjectIdAndPath(projectId, safe);

        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + safe);
        }

        if (!"FILE".equalsIgnoreCase(file.getType())) {
            return; // folders are no-op
        }

        String content = file.getContent() == null ? "" : file.getContent();

        try {
            // 1️⃣ Persist to project filesystem
            Path root = projectRoot(projectId);
            Path resolved = root.resolve(safe).normalize();
            Files.createDirectories(resolved.getParent());
            Files.writeString(
                    resolved,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            // 2️⃣ Persist to session filesystem if active
            sessionRegistry.getSessionIdForProject(projectId)
                    .ifPresent(sessionId -> {
                        try {
                            fileSyncService.writeFileToSession(
                                    sessionId,
                                    safe,
                                    content
                            );
                        } catch (Exception e) {
                            log.warn("Session flush failed", e);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException(
                    "Flush failed for file: " + safe,
                    e
            );
        }
    }
}