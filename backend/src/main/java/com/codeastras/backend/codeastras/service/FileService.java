package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final ProjectFileRepository fileRepo;
    private final ProjectRepository projectRepo;
    private final FileSyncService fileSyncService;
    private final SessionRegistry sessionRegistry;

    public FileService(ProjectFileRepository fileRepo,
                       ProjectRepository projectRepo,
                       FileSyncService fileSyncService,
                       SessionRegistry sessionRegistry) {
        this.fileRepo = fileRepo;
        this.projectRepo = projectRepo;
        this.fileSyncService = fileSyncService;
        this.sessionRegistry = sessionRegistry;
    }

    // -------- Helper Methods --------

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private void requireOwner(Project project, UUID userId) {
        if (project.getOwnerId() == null || !project.getOwnerId().equals(userId)) {
            throw new ForbiddenException("You are not the owner of this project");
        }
    }

    // -------- Main Operations --------

    public List<ProjectFile> findAllByProjectId(UUID projectId, UUID userId) {
        Project project = getProjectOrThrow(projectId);
        requireOwner(project, userId);
        return fileRepo.findByProjectId(projectId);
    }

    public ProjectFile getFile(UUID projectId, String path, UUID userId) {
        Project project = getProjectOrThrow(projectId);
        requireOwner(project, userId);

        ProjectFile file = fileRepo.findByProjectIdAndPath(projectId, path);
        if (file == null) {
            throw new ResourceNotFoundException("File not found: " + path);
        }

        return file;
    }

    // -------- Save File Content --------

    @Transactional
    public ProjectFile saveFileContent(UUID projectId, String path, String content, UUID userId) throws IOException {

        Project project = getProjectOrThrow(projectId);
        requireOwner(project, userId);

        // fetch DB entry
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

        // sync to container session if project is open
        String sessionId = sessionRegistry.getSessionIdByProject(projectId);
        if (sessionId != null) {
            fileSyncService.writeFileToSession(sessionId, path, content);
        }

        return saved;
    }
}
