package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.config.StorageProperties;
import java.nio.charset.StandardCharsets;
import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.codeastras.backend.codeastras.entity.CollaboratorStatus.ACCEPTED;

@Service
public class FileService {

    private final ProjectFileRepository fileRepo;
    private final ProjectRepository projectRepo;
    private final ProjectCollaboratorRepository collaboratorRepo;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;
    private final StorageProperties storageProperties;

    public FileService(ProjectFileRepository fileRepo,
                       ProjectRepository projectRepo,
                       ProjectCollaboratorRepository collaboratorRepo,
                       FileSyncService fileSyncService,
                       SessionRegistry sessionRegistry,
                       StorageProperties storageProperties) {

        this.fileRepo = fileRepo;
        this.projectRepo = projectRepo;
        this.collaboratorRepo = collaboratorRepo;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
        this.storageProperties = storageProperties;
    }

    // ---------------- Helper Methods ----------------

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    /**
     * Checks if user is either:
     *  - Project Owner
     *  - Accepted Collaborator
     */
    private void requireProjectAccess(Project project, UUID userId) {
        UUID ownerId = project.getOwner().getId();
        UUID projectId = project.getId();

        boolean isOwner = ownerId.equals(userId);
        boolean isCollaborator = collaboratorRepo.existsByProjectIdAndUserIdAndStatus(projectId, userId, ACCEPTED);

        if (!isOwner && !isCollaborator) {
            throw new ForbiddenException("You are not authorized to access this project's files.");
        }
    }

    // ---------------- Main Operations ----------------

    public List<ProjectFile> findAllByProjectId(UUID projectId, UUID userId) {
        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project, userId);
        return fileRepo.findByProjectId(projectId);
    }

    public ProjectFile getFile(UUID projectId, String path, UUID userId) {
        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project, userId);

        ProjectFile file = fileRepo.findByProjectIdAndPath(projectId, path);
        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + path);
        }

        return file;
    }

    // ---------------- Save File Content ----------------

    @Transactional
    public ProjectFile saveFileContent(UUID projectId, String path, String content, UUID userId) throws IOException {

        Project project = getProjectOrThrow(projectId);
        requireProjectAccess(project, userId);

        ProjectFile file = fileRepo.findByProjectIdAndPath(projectId, path);
        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + path);
        }

        if (!"FILE".equalsIgnoreCase(file.getType())) {
            throw new IllegalArgumentException("Path is a folder, not a file: " + path);
        }

        // update DB
        file.setContent(content);
        file.setUpdatedAt(Instant.now());
        ProjectFile saved = fileRepo.save(file);

        // === write to project filesystem so FS stays the source-of-truth ===
        Path projectRoot = Paths.get(storageProperties.getProjects()).resolve(projectId.toString()).toAbsolutePath().normalize();
        // ensure project folder exists (repair if needed)
        if (!Files.exists(projectRoot)) {
            // reconstruct from DB (repair)
            // reuse ProjectServiceImpl's repair logic by calling it is nicer, but to avoid circular dependency
            // simply create parent directories here (we assume repair created files earlier or project was created)
            Files.createDirectories(projectRoot);
        }

        Path fileOnDisk = projectRoot.resolve(path).normalize();
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

        // sync to container session if project is open
        String sessionId = sessionRegistry.getSessionIdByProject(projectId);
        if (sessionId != null) {
            fileSyncService.writeFileToSession(sessionId, path, content);
        }

        return saved;
    }

}
