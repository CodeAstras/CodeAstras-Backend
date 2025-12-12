package com.codeastras.backend.codeastras.service;


import com.codeastras.backend.codeastras.entity.CollaboratorStatus;
import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ProjectRepository projectRepo;
    private final ProjectCollaboratorRepository collabRepo;

    public void checkProjectReadAccess(UUID projectId, UUID userId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        boolean isOwner = project.getOwner().getId().equals(userId);
        boolean isAccepted = collabRepo.existsByProjectIdAndUserIdAndStatus(projectId, userId, CollaboratorStatus.ACCEPTED);

        if (!isOwner && !isAccepted) {
            throw new ForbiddenException("You do not have access to this project");
        }
    }

    public void checkProjectWriteAccess(UUID projectId, UUID userId) {
        checkProjectReadAccess(projectId, userId);
    }
}
