package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CollaboratorResponse;
import com.codeastras.backend.codeastras.dto.InviteCollaboratorRequest;
import com.codeastras.backend.codeastras.entity.ProjectCollaborator;
import com.codeastras.backend.codeastras.security.JwtUtils;
import com.codeastras.backend.codeastras.service.ProjectCollaboratorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/collaborators")
@RequiredArgsConstructor
public class CollaboratorController {

    private final ProjectCollaboratorService collabService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<CollaboratorResponse> invite(
            @PathVariable("projectId") UUID projectId,
            @Valid @RequestBody InviteCollaboratorRequest body,
            Authentication authentication
    ) {
        UUID requesterId = (UUID) authentication.getPrincipal();
        ProjectCollaborator collab = collabService.inviteCollaborator(projectId, body.getEmail(), requesterId);

        return ResponseEntity.ok(new CollaboratorResponse(
                collab.getUser().getId(),
                collab.getUser().getEmail(),
                collab.getRole(),
                collab.getStatus(),
                collab.getInvitedAt(),
                collab.getAcceptedAt()
        ));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(
            @PathVariable("projectId") UUID projectId,
            @PathVariable("userId") UUID userId,
            Authentication authentication
    ) {
        UUID requesterId = (UUID) authentication.getPrincipal();
        collabService.removeCollaborator(projectId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
