package com.codeastras.backend.codeastras.dto.collaborator;

import com.codeastras.backend.codeastras.entity.collaborator.CollaboratorRole;
import com.codeastras.backend.codeastras.entity.collaborator.CollaboratorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CollaboratorResponse {

    private UUID id;
    private String nameOrEmail;
    private CollaboratorRole role;
    private CollaboratorStatus status;
    private Instant invitedAt;
    private Instant acceptedAt;

}
