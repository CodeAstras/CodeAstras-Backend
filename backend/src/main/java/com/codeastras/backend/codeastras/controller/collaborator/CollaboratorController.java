package com.codeastras.backend.codeastras.controller.collaborator;

import com.codeastras.backend.codeastras.dto.collaborator.CollaboratorResponse;
import com.codeastras.backend.codeastras.dto.collaborator.InvitationResponse;
import com.codeastras.backend.codeastras.dto.collaborator.InviteCollaboratorRequest;
import com.codeastras.backend.codeastras.dto.collaborator.UpdateCollaboratorRoleRequest;
import com.codeastras.backend.codeastras.entity.collaborator.ProjectCollaborator;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.collaborator.ProjectCollaboratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/collaborators")
@RequiredArgsConstructor
public class CollaboratorController {

    private final ProjectCollaboratorService collabService;

    // INVITE COLLABORATOR (CREATES INVITATION)
    @PostMapping
    public ResponseEntity<InvitationResponse> invite(
            @PathVariable UUID projectId,
            @Valid @RequestBody InviteCollaboratorRequest body,
            Authentication authentication
    ) {
        UUID requesterId = AuthUtil.requireUserId(authentication);

        InvitationResponse response =
                collabService.inviteCollaborator(
                        projectId,
                        body.getEmail(),
                        requesterId
                );

        return ResponseEntity.ok(response);
    }

    // REMOVE COLLABORATOR
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        UUID requesterId = AuthUtil.requireUserId(authentication);
        collabService.removeCollaborator(projectId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    // LIST COLLABORATORS
    @GetMapping
    public ResponseEntity<List<CollaboratorResponse>> listCollaborators(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        UUID requesterId = AuthUtil.requireUserId(authentication);

        return ResponseEntity.ok(
                collabService.listProjectCollaborators(projectId, requesterId)
        );
    }

    // UPDATE ROLE (OWNER ONLY)
    @PatchMapping("/{userId}/role")
    public ResponseEntity<CollaboratorResponse> updateRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateCollaboratorRoleRequest body,
            Authentication authentication
    ) {
        UUID requesterId = AuthUtil.requireUserId(authentication);

        ProjectCollaborator collab =
                collabService.updateRole(
                        projectId,
                        userId,
                        body.getRole(),
                        requesterId
                );

        return ResponseEntity.ok(toResponse(collab));
    }

    // MAPPER
    private CollaboratorResponse toResponse(ProjectCollaborator c) {
        return new CollaboratorResponse(
                c.getUser().getId(),
                c.getUser().getEmail(),
                c.getRole(),
                c.getStatus(),
                c.getInvitedAt(),
                c.getAcceptedAt()
        );
    }
}
