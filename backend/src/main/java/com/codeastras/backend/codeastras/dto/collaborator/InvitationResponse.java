package com.codeastras.backend.codeastras.dto.collaborator;

import com.codeastras.backend.codeastras.entity.collaborator.CollaboratorRole;
import com.codeastras.backend.codeastras.entity.collaborator.InvitationStatus;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID invitationId,
        UUID projectId,
        String projectName,
        String inviterEmail,
        CollaboratorRole role,
        InvitationStatus status,
        Instant invitedAt
) {}