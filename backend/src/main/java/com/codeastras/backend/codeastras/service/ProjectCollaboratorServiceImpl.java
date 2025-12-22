package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CollaboratorResponse;
import com.codeastras.backend.codeastras.entity.*;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
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

    // ==================================================
    // INVITE COLLABORATOR (OWNER ONLY)
    // ==================================================

    @Override
    @Transactional
    public ProjectCollaborator inviteCollaborator(
            UUID projectId,
            String inviteeEmail,
            UUID requesterId
    ) {
        // 🔐 Owner-only
        accessManager.requireOwner(projectId, requesterId);

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        User invitedUser = userRepo.findByEmail(inviteeEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + inviteeEmail
                        ));

        if (invitedUser.getId().equals(requesterId)) {
            throw new IllegalStateException("You cannot invite yourself");
        }

        // 🚫 Prevent duplicate invites except REJECTED
        collabRepo.findByProjectIdAndUserId(projectId, invitedUser.getId())
                .ifPresent(existing -> {
                    if (existing.getStatus() != CollaboratorStatus.REJECTED) {
                        throw new IllegalStateException(
                                "User already invited or is a collaborator"
                        );
                    }
                });

        ProjectCollaborator collab =
                new ProjectCollaborator(
                        project,
                        invitedUser,
                        CollaboratorRole.COLLABORATOR
                );

        collab.setStatus(CollaboratorStatus.PENDING);
        collab.setInvitedAt(Instant.now());
        collab.setAcceptedAt(null);

        return collabRepo.save(collab);
    }

    // ==================================================
    // ACCEPT INVITE (INVITED USER ONLY)
    // ==================================================

    @Override
    @Transactional
    public ProjectCollaborator acceptInvite(UUID projectId, UUID userId) {

        // 🧱 Ensure project still exists
        projectRepo.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        ProjectCollaborator collab =
                collabRepo.findByProjectIdAndUserId(projectId, userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Invite not found"
                                ));

        // 🔒 Defense-in-depth
        if (!collab.getUser().getId().equals(userId)) {
            throw new ForbiddenException(
                    "Cannot accept invite for another user"
            );
        }

        if (collab.getStatus() != CollaboratorStatus.PENDING) {
            throw new IllegalStateException("Invite not pending");
        }

        collab.setStatus(CollaboratorStatus.ACCEPTED);
        collab.setAcceptedAt(Instant.now());

        return collabRepo.save(collab);
    }

    // ==================================================
    // REMOVE COLLABORATOR
    // ==================================================

    @Override
    @Transactional
    public void removeCollaborator(
            UUID projectId,
            UUID targetUserId,
            UUID requesterId
    ) {
        // 🔐 Owner can remove anyone; user can remove self
        if (!requesterId.equals(targetUserId)) {
            accessManager.requireOwner(projectId, requesterId);
        }

        ProjectCollaborator collab =
                collabRepo.findByProjectIdAndUserId(projectId, targetUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Collaborator not found"
                                ));

        collabRepo.delete(collab);
    }

    // ==================================================
    // LIST PROJECT COLLABORATORS
    // ==================================================

    @Override
    public List<CollaboratorResponse> listProjectCollaborators(
            UUID projectId,
            UUID requesterId
    ) {
        // 🔐 Explicit permission
        accessManager.require(
                projectId,
                requesterId,
                ProjectPermission.READ_COLLABORATORS
        );

        return collabRepo.findAllByProjectId(projectId)
                .stream()
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

    // ==================================================
    // LIST USER PROJECTS (ACCEPTED ONLY)
    // ==================================================

    @Override
    public List<CollaboratorResponse> listUserProjects(UUID userId) {

        return collabRepo.findAllByUserId(userId)
                .stream()
                .filter(c ->
                        c.getStatus() == CollaboratorStatus.ACCEPTED)
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

    // ==================================================
    // UPDATE ROLE (OWNER ONLY)
    // ==================================================

    @Override
    @Transactional
    public ProjectCollaborator updateRole(
            UUID projectId,
            UUID targetUserId,
            CollaboratorRole newRole,
            UUID requesterId
    ) {
        // 🔐 Owner-only
        accessManager.requireOwner(projectId, requesterId);

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        if (project.getOwner().getId().equals(targetUserId)) {
            throw new IllegalStateException(
                    "Owner role cannot be changed"
            );
        }

        ProjectCollaborator collab =
                collabRepo.findByProjectIdAndUserId(projectId, targetUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Collaborator not found"
                                ));

        if (collab.getStatus() != CollaboratorStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "Cannot change role of pending collaborator"
            );
        }

        collab.setRole(newRole);
        return collabRepo.save(collab);
    }
}
