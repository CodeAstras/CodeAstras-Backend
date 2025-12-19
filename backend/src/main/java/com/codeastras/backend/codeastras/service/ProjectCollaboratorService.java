package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CollaboratorResponse;
import com.codeastras.backend.codeastras.entity.CollaboratorRole;
import com.codeastras.backend.codeastras.entity.ProjectCollaborator;

import java.util.List;
import java.util.UUID;

public interface ProjectCollaboratorService {
    ProjectCollaborator inviteCollaborator(UUID projectId, String inviteeEmail, UUID requesterId);
    ProjectCollaborator acceptInvite(UUID projectId, UUID userId);
    void removeCollaborator(UUID projectId, UUID userId, UUID requesterId);
    List<CollaboratorResponse> listProjectCollaborators(UUID projectId, UUID requesterId);
    List<CollaboratorResponse> listUserProjects(UUID userId);

    ProjectCollaborator updateRole(
            UUID projectId,
            UUID targetUserId,
            CollaboratorRole newRole,
            UUID requesterId
    );

}
