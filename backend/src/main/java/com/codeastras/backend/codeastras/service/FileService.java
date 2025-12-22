package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.config.StorageProperties;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
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

    private final ProjectFileRepository fileRepo;
    private final ProjectAccessManager accessManager;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;
    private final StorageProperties storageProperties;

    // ==================================================
    // READS
    // ==================================================

    public List<ProjectFile> findAll(UUID projectId, UUID userId) {
        accessManager.require(projectId, userId, ProjectPermission.READ_FILE);
        return fileRepo.findByProjectId(projectId);
    }

    public ProjectFile getFile(UUID projectId, String path, UUID userId) {
        accessManager.require(projectId, userId, ProjectPermission.READ_FILE);

        String safePath = fileSyncService.sanitizeUserPath(path);

        ProjectFile file =
                fileRepo.findByProjectIdAndPath(projectId, safePath);

        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + safePath);
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
    ) {

        String safePath = fileSyncService.sanitizeUserPath(path);
        String t = type.toUpperCase();

        if ("FILE".equals(t)) {
            accessManager.require(projectId, userId, ProjectPermission.CREATE_FILE);
        } else if ("FOLDER".equals(t)) {
            accessManager.require(projectId, userId, ProjectPermission.CREATE_FOLDER);
        } else {
            throw new IllegalArgumentException("Invalid type: " + type);
        }

        if (fileRepo.existsByProjectIdAndPath(projectId, safePath)) {
            throw new IllegalStateException("Already exists: " + safePath);
        }

        ProjectFile pf = new ProjectFile(
                UUID.randomUUID(),
                projectId,
                safePath,
                "FILE".equals(t) ? "" : null,
                t
        );

        pf.setCreatedAt(Instant.now());
        pf.setUpdatedAt(Instant.now());
        fileRepo.save(pf);

        // ---------------- Filesystem (best-effort projection)
        Path resolved = resolveProjectPath(projectId, safePath);

        try {
            if ("FOLDER".equals(t)) {
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
        } catch (IOException e) {
            log.error("Filesystem sync failed for {}", safePath, e);
            // DB is source of truth → do NOT throw
        }

        // ---------------- Session sync (non-blocking)
        sessionRegistry.getSessionIdForProject(projectId)
                .ifPresent(sessionId -> {
                    try {
                        if ("FILE".equals(t)) {
                            fileSyncService.writeFileToSession(
                                    sessionId,
                                    safePath,
                                    ""
                            );
                        }
                    } catch (Exception e) {
                        log.warn("Session sync failed", e);
                    }
                });

        return pf;
    }

    // ==================================================
    // UPDATE
    // ==================================================

    @Transactional
    public ProjectFile save(
            UUID projectId,
            String path,
            String content,
            UUID userId
    ) {

        accessManager.require(projectId, userId, ProjectPermission.UPDATE_FILE);

        String safePath = fileSyncService.sanitizeUserPath(path);

        ProjectFile file =
                fileRepo.findByProjectIdAndPath(projectId, safePath);

        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + safePath);
        }

        if (!"FILE".equalsIgnoreCase(file.getType())) {
            throw new IllegalArgumentException("Not a file");
        }

        // ---------------- DB FIRST (authoritative)
        file.setContent(content);
        file.setUpdatedAt(Instant.now());
        fileRepo.saveAndFlush(file);

        // ---------------- Filesystem SECOND
        Path resolved = resolveProjectPath(projectId, safePath);

        try {
            Files.createDirectories(resolved.getParent());
            Files.writeString(
                    resolved,
                    content == null ? "" : content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            log.error("Filesystem write failed for {}", safePath, e);
            // Do NOT rollback DB
        }

        // ---------------- Session sync LAST
        sessionRegistry.getSessionIdForProject(projectId)
                .ifPresent(sessionId -> {
                    try {
                        fileSyncService.writeFileToSession(
                                sessionId,
                                safePath,
                                content
                        );
                    } catch (Exception e) {
                        log.warn("Session sync failed", e);
                    }
                });

        return file;
    }

    // ==================================================
    // HELPERS
    // ==================================================

    private Path resolveProjectPath(UUID projectId, String safePath) {
        Path root = Paths.get(storageProperties.getProjects())
                .resolve(projectId.toString())
                .toAbsolutePath()
                .normalize();

        Path resolved = root.resolve(safePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path");
        }
        return resolved;
    }
}
