package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CreateProjectRequest;
import com.codeastras.backend.codeastras.dto.ProjectResponse;
import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.entity.User;
import com.codeastras.backend.codeastras.exception.DuplicateResourceException;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final UserRepository userRepository;
    private final SessionRegistry sessionRegistry;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID ownerId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fast, correct, indexed database-level uniqueness check
        if (projectRepository.existsByOwnerIdAndName(ownerId, request.getName())) {
            throw new DuplicateResourceException("Project name already exists");
        }

        // Create Project
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName(request.getName());
        project.setLanguage(request.getLanguage());
        project.setOwnerId(ownerId);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());

        project = projectRepository.save(project);

        // Create default files (folders + files)
        List<ProjectFile> initialFiles = createDefaultFiles(project.getId());
        projectFileRepository.saveAll(initialFiles);

        return ProjectResponse.from(project, initialFiles, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        // Authorization: owner only for now (if you have project membership later, update)
        if (project.getOwnerId() == null || !project.getOwnerId().equals(requesterId)) {
            throw new ForbiddenException("You are not authorized to access this project");
        }

        List<ProjectFile> files = projectFileRepository.findByProjectId(projectId);

        // Check active session
        SessionRegistry.SessionInfo s = sessionRegistry.getByProject(projectId);
        String activeSessionId = s == null ? null : s.sessionId;

        return ProjectResponse.from(project, files, activeSessionId);
    }

    private List<ProjectFile> createDefaultFiles(UUID projectId) {
        List<ProjectFile> files = new ArrayList<>();

        // create src folder entry
        files.add(new ProjectFile(
                UUID.randomUUID(),
                projectId,
                "src",
                null,
                "FOLDER"
        ));

        // main.py
        files.add(new ProjectFile(
                UUID.randomUUID(),
                projectId,
                "src/main.py",
                "print('Hello from CodeAstra')",
                "FILE"
        ));

        // README
        files.add(new ProjectFile(
                UUID.randomUUID(),
                projectId,
                "README.md",
                "# " + "New Project",
                "FILE"
        ));

        return files;
    }

    @Override
    public List<ProjectResponse> getProjectsForUser(UUID ownerId) {

        List<Project> projects = projectRepository.findByOwnerId(ownerId);

        return projects.stream()
                .map(project -> {
                    var files = projectFileRepository.findByProjectId(project.getId());

                    String activeSessionId = sessionRegistry
                            .getSessionIdForProject(project.getId())
                            .orElse(null);

                    return ProjectResponse.from(project, files, activeSessionId);
                })
                .toList();
    }
}
