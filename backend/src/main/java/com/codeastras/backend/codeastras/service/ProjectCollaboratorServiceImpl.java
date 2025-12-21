package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CollaboratorResponse;
import com.codeastras.backend.codeastras.entity.*;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectCollaboratorServiceImpl implements ProjectCollaboratorService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final ProjectCollaboratorRepository collabRepo;
    private final ProjectAccessManager accessManager;

    @Override
    @Transactional
    public ProjectCollaborator inviteCollaborator(UUID projectId, String inviteeEmail, UUID requesterId) {
        Project project = accessManager.requireOwner(projectId, requesterId);

        User invitedUser = userRepo.findByEmail(inviteeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + inviteeEmail));

        if (invitedUser.getId().equals(requesterId)) {
            throw new IllegalStateException("You cannot invite yourself");
        }

        collabRepo.findByProjectIdAndUserId(projectId, invitedUser.getId()).ifPresent(existing -> {
            throw new IllegalStateException("User already invited or is a collaborator");
        });

        ProjectCollaborator collab = new ProjectCollaborator(project, invitedUser, CollaboratorRole.COLLABORATOR);
        collab.setStatus(CollaboratorStatus.PENDING);
        collab.setInvitedAt(Instant.now());
        return collabRepo.save(collab);
    }

    @Override
    @Transactional
    public ProjectCollaborator acceptInvite(UUID projectId, UUID userId) {
        ProjectCollaborator collab = collabRepo.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        if (collab.getStatus() != CollaboratorStatus.PENDING) {
            throw new IllegalStateException("Invite not pending");
        }

        collab.setStatus(CollaboratorStatus.ACCEPTED);
        collab.setAcceptedAt(Instant.now());
        return collabRepo.save(collab);
    }

    @Override
    @Transactional
    public void removeCollaborator(UUID projectId, UUID userId, UUID requesterId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        boolean isOwner = project.getOwner().getId().equals(requesterId);
        boolean removingSelf = requesterId.equals(userId);

        if (!isOwner && !removingSelf) {
            throw new ForbiddenException("Not authorized to remove this collaborator");
        }

        ProjectCollaborator collab = collabRepo.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found"));

        collabRepo.delete(collab);
    }

    @Override
    public List<CollaboratorResponse> listProjectCollaborators(UUID projectId, UUID requesterId) {
        accessManager.requireRead(projectId, requesterId);

        return collabRepo.findAllByProjectId(projectId).stream()
                .map(c -> new CollaboratorResponse(
                        c.getUser().getId(),
                        c.getUser().getEmail(),
                        c.getRole(),
                        c.getStatus(),
                        c.getInvitedAt(),
                        c.getAcceptedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<CollaboratorResponse> listUserProjects(UUID userId) {

        return collabRepo.findAllByUserId(userId).stream()
                .filter(c -> c.getStatus() == CollaboratorStatus.ACCEPTED)
                .map(c -> new CollaboratorResponse(
                        c.getProject().getId(),
                        c.getProject().getName(),
                        c.getRole(),
                        c.getStatus(),
                        c.getInvitedAt(),
                        c.getAcceptedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectCollaborator updateRole(
            UUID projectId,
            UUID targetUserId,
            CollaboratorRole newRole,
            UUID requesterId
    ) {
        accessManager.requireOwner(projectId, requesterId);

        // Prevent owner role mutation
        if (projectRepo.findById(projectId).get().getOwner().getId().equals(targetUserId)) {
            throw new IllegalStateException("Owner role cannot be changed");
        }

        ProjectCollaborator collab = collabRepo
                .findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found"));

        if (collab.getStatus() != CollaboratorStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot change role of pending collaborator");
        }

        collab.setRole(newRole);
        return collabRepo.save(collab);
    }


}
