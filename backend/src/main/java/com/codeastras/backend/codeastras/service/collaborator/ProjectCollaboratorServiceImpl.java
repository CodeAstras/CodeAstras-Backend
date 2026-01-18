package com.codeastras.backend.codeastras.service.collaborator;

import com.codeastras.backend.codeastras.entity.auth.User;
import com.codeastras.backend.codeastras.entity.collaborator.*;
import com.codeastras.backend.codeastras.entity.collaborator.CollaboratorStatus;
import com.codeastras.backend.codeastras.entity.project.Project;
import com.codeastras.backend.codeastras.websocket.publisher.InviteWebSocketPublisher;
import com.codeastras.backend.codeastras.dto.collaborator.CollaboratorResponse;
import com.codeastras.backend.codeastras.dto.collaborator.InvitationResponse;
import com.codeastras.backend.codeastras.dto.collaborator.InviteNotification;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.collaborator.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.repository.collaborator.ProjectInvitationRepository;
import com.codeastras.backend.codeastras.repository.project.ProjectRepository;
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
        private final ProjectInvitationRepository invitationRepo;
        private final ProjectAccessManager accessManager;
        private final InviteWebSocketPublisher inviteWsPublisher;

        // INVITE COLLABORATOR (OWNER ONLY)
        @Override
        @Transactional
        public InvitationResponse inviteCollaborator(
                        UUID projectId,
                        String inviteeEmail,
                        UUID requesterId) {
                accessManager.requireOwner(projectId, requesterId);

                Project project = projectRepo.findById(projectId)
                                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

                User inviter = userRepo.findById(requesterId)
                                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found"));

                User invitedUser = userRepo.findByEmail(inviteeEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with email: " + inviteeEmail));

                if (invitedUser.getId().equals(requesterId)) {
                        throw new IllegalStateException("You cannot invite yourself");
                }

                collabRepo.findByProjectIdAndUserId(projectId, invitedUser.getId())
                                .ifPresent(c -> {
                                        throw new IllegalStateException("User is already a collaborator");
                                });

                invitationRepo.findByProjectIdAndInviteeId(projectId, invitedUser.getId())
                                .ifPresent(i -> {
                                        if (i.getStatus() == InvitationStatus.PENDING) {
                                                throw new IllegalStateException(
                                                                "User already has a pending invitation");
                                        }
                                });

                ProjectInvitation invite = new ProjectInvitation();
                invite.setProject(project);
                invite.setInviter(inviter);
                invite.setInvitee(invitedUser);
                invite.setRole(CollaboratorRole.COLLABORATOR);
                invite.setStatus(InvitationStatus.PENDING);
                invite.setCreatedAt(Instant.now());

                ProjectInvitation saved = invitationRepo.save(invite);

                // 🔔 REAL-TIME NOTIFICATION (BEFORE RETURN)
                inviteWsPublisher.notifyUser(
                                invitedUser.getId(),
                                new InviteNotification(
                                                "INVITE_SENT",
                                                saved.getId(),
                                                project.getId(),
                                                project.getName(),
                                                inviter.getEmail()));

                return new InvitationResponse(
                                saved.getId(),
                                project.getId(),
                                project.getName(),
                                inviter.getEmail(),
                                saved.getRole(),
                                saved.getStatus(),
                                saved.getCreatedAt());
        }

        // ACCEPT INVITATION
        @Override
        @Transactional
        public ProjectCollaborator acceptInvitation(UUID invitationId, UUID userId) {

                ProjectInvitation invite = invitationRepo.findById(invitationId)
                                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

                if (!invite.getInvitee().getId().equals(userId)) {
                        throw new ForbiddenException("Not your invitation");
                }

                if (invite.getStatus() != InvitationStatus.PENDING) {
                        throw new IllegalStateException("Invitation already resolved");
                }

                collabRepo.findByProjectIdAndUserId(invite.getProject().getId(), userId)
                                .ifPresent(c -> {
                                        throw new IllegalStateException("Already a collaborator");
                                });

                invite.setStatus(InvitationStatus.ACCEPTED);
                invite.setRespondedAt(Instant.now());
                invitationRepo.save(invite);

                ProjectCollaborator collaborator = new ProjectCollaborator(invite.getProject(), invite.getInvitee(),
                                invite.getRole());

                collaborator.setStatus(CollaboratorStatus.ACCEPTED);
                collaborator.setInvitedAt(invite.getCreatedAt());
                collaborator.setAcceptedAt(Instant.now());

                ProjectCollaborator saved = collabRepo.save(collaborator);

                // 🔔 Notify inviter
                inviteWsPublisher.notifyUser(
                                invite.getInviter().getId(),
                                new InviteNotification(
                                                "INVITE_ACCEPTED",
                                                invite.getId(),
                                                invite.getProject().getId(),
                                                invite.getProject().getName(),
                                                invite.getInvitee().getEmail()));

                return saved;
        }

        // REJECT INVITATION
        @Override
        @Transactional
        public void rejectInvitation(UUID invitationId, UUID userId) {

                ProjectInvitation invite = invitationRepo.findById(invitationId)
                                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

                if (!invite.getInvitee().getId().equals(userId)) {
                        throw new ForbiddenException("Not your invitation");
                }

                if (invite.getStatus() != InvitationStatus.PENDING) {
                        throw new IllegalStateException("Invitation already resolved");
                }

                invite.setStatus(InvitationStatus.REJECTED);
                invite.setRespondedAt(Instant.now());
                invitationRepo.save(invite);

                // 🔔 Notify inviter
                inviteWsPublisher.notifyUser(
                                invite.getInviter().getId(),
                                new InviteNotification(
                                                "INVITE_REJECTED",
                                                invite.getId(),
                                                invite.getProject().getId(),
                                                invite.getProject().getName(),
                                                invite.getInvitee().getEmail()));
        }

        // REMOVE COLLABORATOR
        @Override
        @Transactional
        public void removeCollaborator(UUID projectId, UUID targetUserId, UUID requesterId) {
                if (!requesterId.equals(targetUserId)) {
                        accessManager.requireOwner(projectId, requesterId);
                }

                // 1. Try to find accepted collaborator
                var collabOpt = collabRepo.findByProjectIdAndUserId(projectId, targetUserId);
                if (collabOpt.isPresent()) {
                        ProjectCollaborator collab = collabOpt.get();
                        if (collab.getRole() == CollaboratorRole.OWNER) {
                                long ownerCount = collabRepo.countByProjectIdAndRole(
                                                projectId,
                                                CollaboratorRole.OWNER);
                                if (ownerCount <= 1) { // Changed to 1 to match updateRole logic
                                        throw new IllegalStateException("Project must have at least one Owner");
                                }
                        }
                        collabRepo.delete(collab);
                        return;
                }

                // 2. If not found, try to find pending invitation
                var inviteOpt = invitationRepo.findByProjectIdAndInviteeId(projectId, targetUserId);
                if (inviteOpt.isPresent()) {
                        invitationRepo.delete(inviteOpt.get());
                        return;
                }

                // 3. If neither
                throw new ResourceNotFoundException("Collaborator or invitation not found");
        }

        // LIST PROJECT COLLABORATORS
        public List<CollaboratorResponse> listProjectCollaborators(
                        UUID projectId,
                        UUID requesterId) {
                accessManager.require(
                                projectId,
                                requesterId,
                                ProjectPermission.READ_COLLABORATORS);

                // 1. Get accepted collaborators
                List<CollaboratorResponse> collaborators = collabRepo.findAllByProjectId(projectId)
                                .stream()
                                .map(c -> new CollaboratorResponse(
                                                c.getUser().getId(),
                                                c.getUser().getEmail(),
                                                c.getRole(),
                                                c.getStatus(),
                                                c.getInvitedAt(),
                                                c.getAcceptedAt()))
                                .collect(Collectors.toList());

                // 2. Get pending invitations
                List<CollaboratorResponse> pending = invitationRepo
                                .findAllByProjectIdAndStatus(projectId, InvitationStatus.PENDING)
                                .stream()
                                .map(i -> new CollaboratorResponse(
                                                i.getInvitee().getId(),
                                                i.getInvitee().getEmail(),
                                                i.getRole(),
                                                CollaboratorStatus.PENDING, // Explicitly map to PENDING status
                                                i.getCreatedAt(),
                                                null))
                                .collect(Collectors.toList());

                // 3. Merge
                collaborators.addAll(pending);
                return collaborators;
        }

        // LIST USER PROJECTS (ACCEPTED ONLY)
        @Override
        public List<CollaboratorResponse> listUserProjects(UUID userId) {

                return collabRepo.findAllByUserId(userId)
                                .stream()
                                .filter(c -> c.getStatus() == CollaboratorStatus.ACCEPTED)
                                .map(c -> new CollaboratorResponse(
                                                c.getProject().getId(),
                                                c.getProject().getName(),
                                                c.getRole(),
                                                c.getStatus(),
                                                c.getInvitedAt(),
                                                c.getAcceptedAt()))
                                .collect(Collectors.toList());
        }

        // UPDATE ROLE (OWNER ONLY)
        @Override
        @Transactional
        public ProjectCollaborator updateRole(
                        UUID projectId,
                        UUID targetUserId,
                        CollaboratorRole newRole,
                        UUID requesterId) {
                accessManager.requireOwner(projectId, requesterId);

                ProjectCollaborator collab = collabRepo.findByProjectIdAndUserId(projectId, targetUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found"));

                if (collab.getStatus() != CollaboratorStatus.ACCEPTED) {
                        throw new IllegalStateException("Cannot change role of pending collaborator");
                }

                collab.setRole(newRole);

                if (collab.getRole() == CollaboratorRole.OWNER
                                && newRole != CollaboratorRole.OWNER) {

                        long ownerCount = collabRepo.countByProjectIdAndRole(
                                        projectId,
                                        CollaboratorRole.OWNER);

                        if (ownerCount <= 1) {
                                throw new IllegalStateException(
                                                "Cannot demote the last project owner");
                        }
                }

                return collabRepo.save(collab);
        }

}
