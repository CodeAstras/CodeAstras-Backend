package com.codeastras.backend.codeastras.controller.collaborator;

import com.codeastras.backend.codeastras.dto.collaborator.InvitationResponse;
import com.codeastras.backend.codeastras.entity.collaborator.InvitationStatus;
import com.codeastras.backend.codeastras.repository.collaborator.ProjectInvitationRepository;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.collaborator.ProjectCollaboratorServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me/invites")
@RequiredArgsConstructor
public class UserInviteController {

    private final ProjectInvitationRepository invitationRepo;
    private final ProjectCollaboratorServiceImpl collabService;

    // LIST MY PENDING INVITES
    @GetMapping
    public ResponseEntity<List<InvitationResponse>> myInvites(
            Authentication authentication
    ) {
        UUID userId = AuthUtil.requireUserId(authentication);

        List<InvitationResponse> invites =
                invitationRepo
                        .findAllByInviteeIdAndStatus(
                                userId,
                                InvitationStatus.PENDING
                        )
                        .stream()
                        .map(invite -> new InvitationResponse(
                                invite.getId(),
                                invite.getProject().getId(),
                                invite.getProject().getName(),
                                invite.getInviter().getEmail(),
                                invite.getRole(),
                                invite.getStatus(),
                                invite.getCreatedAt()
                        ))
                        .toList();

        return ResponseEntity.ok(invites);
    }

    // ACCEPT INVITE
    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<Void> accept(
            @PathVariable UUID invitationId,
            Authentication authentication
    ) {
        UUID userId = AuthUtil.requireUserId(authentication);
        collabService.acceptInvitation(invitationId, userId);
        return ResponseEntity.noContent().build();
    }

    // REJECT INVITE
    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable UUID invitationId,
            Authentication authentication
    ) {
        UUID userId = AuthUtil.requireUserId(authentication);
        collabService.rejectInvitation(invitationId, userId);
        return ResponseEntity.noContent().build();
    }
}
