package com.codeastras.backend.codeastras.controller.collaborator;

import com.codeastras.backend.codeastras.dto.collaborator.CollaboratorResponse;
import com.codeastras.backend.codeastras.service.collaborator.ProjectCollaboratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/me/collaborations")
@RequiredArgsConstructor
public class UserCollaborationController {

    private final ProjectCollaboratorService collabService;

    @GetMapping
    public ResponseEntity<List<CollaboratorResponse>> myProjects(
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(collabService.listUserProjects(userId));
    }

}
