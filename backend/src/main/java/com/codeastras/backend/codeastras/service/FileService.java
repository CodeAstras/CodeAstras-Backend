package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.config.StorageProperties;
import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.store.SessionRegistry;
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

import static com.codeastras.backend.codeastras.entity.CollaboratorStatus.ACCEPTED;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    public static final String ENTRY_FILE = "src/main.py";

    private final ProjectFileRepository fileRepo;
    private final ProjectRepository projectRepo;
    private final ProjectCollaboratorRepository collaboratorRepo;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;
    private final StorageProperties storageProperties;

    public FileService(
            ProjectFileRepository fileRepo,
            ProjectRepository projectRepo,
            ProjectCollaboratorRepository collaboratorRepo,
            FileSyncService fileSyncService,
            SessionRegistry sessionRegistry,
            StorageProperties storageProperties
    ) {
        this.fileRepo = fileRepo;
        this.projectRepo = projectRepo;
        this.collaboratorRepo = collaboratorRepo;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
        this.storageProperties = storageProperties;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private void requireProjectAccess(Project project, UUID userId) {
        boolean isOwner = project.getOwner().getId().equals(userId);
        boolean isCollaborator =
                collaboratorRepo.existsByProjectIdAndUserIdAndStatus(
                        project.getId(), userId, ACCEPTED);

        if (!isOwner && !isCollaborator) {
            throw new ForbiddenException("You are not authorized to access this project");
        }
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        if (path.contains("..")) {
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

    // ----------------------------------------------------------------
    // Reads
    // ----------------------------------------------------------------

    public List<ProjectFile> findAllByProjectId(UUID projectId, UUID userId) {
        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project, userId);
        return fileRepo.findByProjectId(projectId);
    }

    public ProjectFile getFile(UUID projectId, String path, UUID userId) {
        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project, userId);

        String safePath = normalizePath(path);

        ProjectFile file = fileRepo.findByProjectIdAndPath(projectId, safePath);
        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + safePath);
        }

        return file;
    }

    // ----------------------------------------------------------------
    // 🔥 NEW: CREATE FILE / FOLDER
    // ----------------------------------------------------------------

    @Transactional
    public ProjectFile createFile(
            UUID projectId,
            String path,
            String type,
            UUID userId
    ) throws IOException {

        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project, userId);

        String safePath = normalizePath(path);
        String upperType = type.toUpperCase();

        if (!upperType.equals("FILE") && !upperType.equals("FOLDER")) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }

        if (fileRepo.findByProjectIdAndPath(projectId, safePath) != null) {
            throw new IllegalStateException("Path already exists: " + safePath);
        }

        // 1️⃣ Create DB record
        ProjectFile file = new ProjectFile(
                UUID.randomUUID(),
                projectId,
                safePath,
                upperType.equals("FILE") ? "" : null,
                upperType
        );
        file.setCreatedAt(Instant.now());
        file.setUpdatedAt(Instant.now());

        ProjectFile saved = fileRepo.save(file);

        // 2️⃣ Create on disk
        Path root = projectRoot(projectId);
        Files.createDirectories(root);

        Path resolved = root.resolve(safePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path");
        }

        if (upperType.equals("FOLDER")) {
            Files.createDirectories(resolved);
        } else {
            if (resolved.getParent() != null) {
                Files.createDirectories(resolved.getParent());
            }
            Files.writeString(
                    resolved,
                    "",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );
        }

        // 3️⃣ Sync to active session (if any)
        sessionRegistry.getSessionIdForProject(projectId).ifPresent(sessionId -> {
            try {
                if (upperType.equals("FILE")) {
                    fileSyncService.writeFileToSession(sessionId, safePath, "");
                }
            } catch (Exception e) {
                log.warn("Failed to sync new file to session", e);
            }
        });

        return saved;
    }

    // ----------------------------------------------------------------
    // Writes
    // ----------------------------------------------------------------

    @Transactional
    public ProjectFile saveFileContent(
            UUID projectId,
            String path,
            String content,
            UUID userId
    ) throws IOException {

        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project, userId);

        String safePath = normalizePath(path);

        ProjectFile file = fileRepo.findByProjectIdAndPath(projectId, safePath);
        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + safePath);
        }

        if (!"FILE".equalsIgnoreCase(file.getType())) {
            throw new IllegalArgumentException("Cannot write to folder: " + safePath);
        }

        // 1️⃣ Write to disk
        Path root = projectRoot(projectId);
        Files.createDirectories(root);

        Path fileOnDisk = root.resolve(safePath).normalize();
        if (!fileOnDisk.startsWith(root)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        if (fileOnDisk.getParent() != null) {
            Files.createDirectories(fileOnDisk.getParent());
        }

        Files.writeString(
                fileOnDisk,
                content == null ? "" : content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        // 2️⃣ Update DB
        file.setContent(content);
        file.setUpdatedAt(Instant.now());
        ProjectFile saved = fileRepo.save(file);

        // 3️⃣ Sync to active session
        sessionRegistry.getSessionIdForProject(projectId).ifPresent(sessionId -> {
            try {
                fileSyncService.writeFileToSession(sessionId, safePath, content);
            } catch (Exception e) {
                log.warn("Failed to sync file to session", e);
            }
        });

        return saved;
    }
}
