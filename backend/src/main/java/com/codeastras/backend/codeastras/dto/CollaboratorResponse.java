package com.codeastras.backend.codeastras.dto;

import com.codeastras.backend.codeastras.entity.CollaboratorRole;
import com.codeastras.backend.codeastras.entity.CollaboratorStatus;
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
