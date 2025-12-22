package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.config.StorageProperties;
import com.codeastras.backend.codeastras.dto.CreateProjectRequest;
import com.codeastras.backend.codeastras.dto.ProjectResponse;
import com.codeastras.backend.codeastras.entity.*;
import com.codeastras.backend.codeastras.exception.DuplicateResourceException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.*;
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
import java.util.*;

import static com.codeastras.backend.codeastras.entity.CollaboratorStatus.ACCEPTED;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final Logger log =
            LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final UserRepository userRepository;
    private final ProjectCollaboratorRepository collaboratorRepo;
    private final SessionRegistry sessionRegistry;
    private final StorageProperties storageProperties;
    private final ProjectAccessManager accessManager;

    // ---------------------------------------------------------
    // CREATE PROJECT
    // ---------------------------------------------------------

    @Override
    @Transactional
    public ProjectResponse createProject(
            CreateProjectRequest request,
            UUID ownerId
    ) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        validateProjectName(request.getName());

        if (projectRepository.existsByOwner_IdAndName(ownerId, request.getName())) {
            throw new DuplicateResourceException("Project name already exists");
        }

        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName(request.getName());
        project.setLanguage(request.getLanguage());
        project.setOwner(owner);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());

        project = projectRepository.save(project);

        // ---------------- DEFAULT FILES
        List<ProjectFile> initialFiles =
                createDefaultFiles(project.getId());

        projectFileRepository.saveAll(initialFiles);
        createProjectRootOnDisk(project.getId(), initialFiles);

        // ---------------- OWNER AS COLLABORATOR (CRITICAL)
        registerOwnerCollaborator(project, owner);

        return ProjectResponse.from(project, initialFiles, null);
    }

    // ---------------------------------------------------------
    // GET PROJECT
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(
            UUID projectId,
            UUID requesterId
    ) {
        accessManager.require(
                projectId,
                requesterId,
                ProjectPermission.READ_FILE
        );

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found: " + projectId));

        List<ProjectFile> files =
                projectFileRepository.findByProjectId(projectId);

        SessionRegistry.SessionInfo session =
                sessionRegistry.getByProject(projectId);

        String activeSessionId =
                session == null ? null : session.getSessionId();

        return ProjectResponse.from(project, files, activeSessionId);
    }

    // ---------------------------------------------------------
    // LIST USER PROJECTS
    // ---------------------------------------------------------

    @Override
    public List<ProjectResponse> getProjectsForUser(UUID userId) {

        Objects.requireNonNull(userId, "userId must not be null");

        List<Project> owned =
                projectRepository.findByOwner_Id(userId);

        List<ProjectCollaborator> collabs =
                collaboratorRepo.findAllByUserId(userId)
                        .stream()
                        .filter(c -> c.getStatus() == ACCEPTED)
                        .toList();

        Set<UUID> seen = new HashSet<>();
        List<ProjectResponse> result = new ArrayList<>();

        for (Project project : owned) {
            seen.add(project.getId());
            result.add(toResponse(project));
        }

        for (ProjectCollaborator c : collabs) {
            Project project = c.getProject();
            if (seen.add(project.getId())) {
                result.add(toResponse(project));
            }
        }

        return result;
    }

    // ---------------------------------------------------------
    // INTERNAL HELPERS
    // ---------------------------------------------------------

    private void registerOwnerCollaborator(Project project, User owner) {

        ProjectCollaborator ownerRow =
                new ProjectCollaborator(
                        project,
                        owner,
                        CollaboratorRole.OWNER
                );

        ownerRow.setStatus(ACCEPTED);
        ownerRow.setAcceptedAt(Instant.now());

        collaboratorRepo.save(ownerRow);
    }

    private ProjectResponse toResponse(Project project) {

        List<ProjectFile> files =
                projectFileRepository.findByProjectId(project.getId());

        String activeSessionId =
                sessionRegistry
                        .getSessionIdForProject(project.getId())
                        .orElse(null);

        return ProjectResponse.from(project, files, activeSessionId);
    }

    private void validateProjectName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Project name too long");
        }
        if (!name.matches("[A-Za-z0-9_\\- ]+")) {
            throw new IllegalArgumentException(
                    "Project name contains invalid characters");
        }
    }

    private void createProjectRootOnDisk(
            UUID projectId,
            List<ProjectFile> initialFiles
    ) {
        Path projectRoot =
                Paths.get(storageProperties.getProjects())
                        .resolve(projectId.toString());

        try {
            Files.createDirectories(projectRoot);

            for (ProjectFile f : initialFiles) {

                Path p = projectRoot.resolve(f.getPath());

                if ("FOLDER".equals(f.getType())) {
                    Files.createDirectories(p);
                } else {
                    if (p.getParent() != null) {
                        Files.createDirectories(p.getParent());
                    }
                    Files.writeString(
                            p,
                            f.getContent() == null ? "" : f.getContent(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING
                    );
                }
            }

            log.info(
                    "Project {} created at {}",
                    projectId,
                    projectRoot
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to create project directory structure",
                    e
            );
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
}
