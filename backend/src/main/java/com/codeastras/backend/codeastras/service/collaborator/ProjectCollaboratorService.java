package com.codeastras.backend.codeastras.service.collaborator;

import com.codeastras.backend.codeastras.dto.collaborator.CollaboratorResponse;
import com.codeastras.backend.codeastras.dto.collaborator.InvitationResponse;
import com.codeastras.backend.codeastras.entity.collaborator.CollaboratorRole;
import com.codeastras.backend.codeastras.entity.collaborator.ProjectCollaborator;

import java.util.List;
import java.util.UUID;

public interface ProjectCollaboratorService {

    InvitationResponse inviteCollaborator(
            UUID projectId,
            String inviteeEmail,
            UUID requesterId
    );

    ProjectCollaborator acceptInvitation(
            UUID invitationId,
            UUID userId
    );

    void rejectInvitation(
            UUID invitationId,
            UUID userId
    );

    void removeCollaborator(
            UUID projectId,
            UUID targetUserId,
            UUID requesterId
    );

    List<CollaboratorResponse> listProjectCollaborators(
            UUID projectId,
            UUID requesterId
    );

    List<CollaboratorResponse> listUserProjects(
            UUID userId
    );

    ProjectCollaborator updateRole(
            UUID projectId,
            UUID targetUserId,
            CollaboratorRole newRole,
            UUID requesterId
    );
}
