package com.codeastras.backend.codeastras.dto.collaborator;

import java.util.UUID;

public record InviteNotification(
        String type,           // INVITE_SENT | INVITE_ACCEPTED | INVITE_REJECTED
        UUID invitationId,
        UUID projectId,
        String projectName,
        String actorEmail      // who triggered this event
) {}

