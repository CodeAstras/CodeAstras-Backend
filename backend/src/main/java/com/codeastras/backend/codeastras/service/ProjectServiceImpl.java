package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.config.StorageProperties;
import com.codeastras.backend.codeastras.dto.CreateProjectRequest;
import com.codeastras.backend.codeastras.dto.ProjectResponse;
import com.codeastras.backend.codeastras.entity.*;
import com.codeastras.backend.codeastras.exception.DuplicateResourceException;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.*;
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
import java.util.*;

import static com.codeastras.backend.codeastras.entity.CollaboratorStatus.ACCEPTED;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final UserRepository userRepository;
    private final ProjectCollaboratorRepository collaboratorRepo;
    private final SessionRegistry sessionRegistry;
    private final StorageProperties storageProperties;

    // ---------------------------------------------------------
    // CREATE PROJECT
    // ---------------------------------------------------------
    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID ownerId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateProjectName(request.getName());

        if (projectRepository.existsByOwner_IdAndName(ownerId, request.getName())) {
            throw new DuplicateResourceException("Project name already exists");
        }

        // Create project entity
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName(request.getName());
        project.setLanguage(request.getLanguage());
        project.setOwner(owner);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());

        project = projectRepository.save(project);

        // Create project files (DB + FS)
        List<ProjectFile> initialFiles = createDefaultFiles(project.getId());
        projectFileRepository.saveAll(initialFiles);

        createProjectRootOnDisk(project.getId(), initialFiles);

        // Register owner as collaborator
        ProjectCollaborator ownerRow = new ProjectCollaborator(project, owner, CollaboratorRole.OWNER);
        ownerRow.setStatus(ACCEPTED);
        collaboratorRepo.save(ownerRow);

        return ProjectResponse.from(project, initialFiles, null);
    }

    // ---------------------------------------------------------
    // GET PROJECT
    // ---------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId, UUID requesterId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        boolean isOwner = project.getOwner().getId().equals(requesterId);
        boolean isCollaborator = collaboratorRepo.existsByProjectIdAndUserIdAndStatus(projectId, requesterId, ACCEPTED);

        if (!isOwner && !isCollaborator) {
            throw new ForbiddenException("Not authorized");
        }

        List<ProjectFile> files = projectFileRepository.findByProjectId(projectId);

        SessionRegistry.SessionInfo s = sessionRegistry.getByProject(projectId);
        String activeSessionId = (s == null) ? null : s.getSessionId();

        return ProjectResponse.from(project, files, activeSessionId);
    }

    // ---------------------------------------------------------
    // LIST USER PROJECTS
    // ---------------------------------------------------------
    @Override
    public List<ProjectResponse> getProjectsForUser(UUID ownerId) {

        List<Project> projects = projectRepository.findByOwner_Id(ownerId);

        return projects.stream()
                .map(project -> {
                    List<ProjectFile> files = projectFileRepository.findByProjectId(project.getId());

                    String activeSessionId = sessionRegistry
                            .getSessionIdForProject(project.getId())
                            .orElse(null);

                    return ProjectResponse.from(project, files, activeSessionId);
                })
                .toList();
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private void validateProjectName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Project name too long");
        }
        if (!name.matches("[A-Za-z0-9_\\- ]+")) {
            throw new IllegalArgumentException("Project name contains invalid characters");
        }
    }

    private void createProjectRootOnDisk(UUID projectId, List<ProjectFile> initialFiles) {
        Path projectRoot = Paths.get(storageProperties.getProjects()).resolve(projectId.toString());

        try {
            Files.createDirectories(projectRoot);

            for (ProjectFile f : initialFiles) {
                Path p = projectRoot.resolve(f.getPath());

                if (f.getType().equals("FOLDER")) {
                    Files.createDirectories(p);
                } else {
                    if (p.getParent() != null) Files.createDirectories(p.getParent());
                    Files.writeString(
                            p,
                            f.getContent() == null ? "" : f.getContent(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING
                    );
                }
            }

            log.info("Project {} created at {}", projectId, projectRoot);

        } catch (IOException e) {
            throw new RuntimeException("Failed to create project directory structure", e);
        }
    }

    private List<ProjectFile> createDefaultFiles(UUID projectId) {
        List<ProjectFile> files = new ArrayList<>();

        files.add(new ProjectFile(
                UUID.randomUUID(),
                projectId,
                "src",
                null,
                "FOLDER"
        ));

        files.add(new ProjectFile(
                UUID.randomUUID(),
                projectId,
                "src/main.py",
                "print('Hello from CodeAstra')",
                "FILE"
        ));

        files.add(new ProjectFile(
                UUID.randomUUID(),
                projectId,
                "README.md",
                "# New Project\n\nThis project was created in CodeAstra.",
                "FILE"
        ));

        return files;
    }

    @Override
    @Transactional
    public void repairProjectFilesystemIfMissing(UUID projectId) {

        Path projectRoot = Paths.get(storageProperties.getProjects()).resolve(projectId.toString());

        // If directory exists, nothing to do
        if (Files.exists(projectRoot)) {
            return;
        }

        // Read DB files and recreate from scratch
        List<ProjectFile> files = projectFileRepository.findByProjectId(projectId);

        if (files == null || files.isEmpty()) {
            // create a default skeleton instead of failing
            List<ProjectFile> defaults = createDefaultFiles(projectId);
            projectFileRepository.saveAll(defaults);
            createProjectRootOnDisk(projectId, defaults);
            log.warn("No DB files for project {}; created default skeleton", projectId);
            return;
        }

        createProjectRootOnDisk(projectId, files);
        log.warn("Repaired missing filesystem for project {}", projectId);
    }


}
