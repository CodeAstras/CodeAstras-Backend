package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.entity.CollaboratorStatus;
import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectAccessManager {

    private final ProjectRepository projectRepo;
    private final ProjectCollaboratorRepository collabRepo;

    public Project requireRead(UUID projectId, UUID userId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (project.getOwner().getId().equals(userId)) return project;

        boolean accepted = collabRepo.existsByProjectIdAndUserIdAndStatus(
                projectId, userId, CollaboratorStatus.ACCEPTED
        );

        if (!accepted) {
            throw new ForbiddenException("No access to project");
        }

        return project;
    }

    public Project requireWrite(UUID projectId, UUID userId) {
        return requireRead(projectId, userId);
    }

    public Project requireOwner(UUID projectId, UUID userId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only owner allowed");
        }

        return project;
    }
}
