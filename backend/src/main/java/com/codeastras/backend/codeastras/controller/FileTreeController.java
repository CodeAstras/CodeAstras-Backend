package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.FileNodeDto;
import com.codeastras.backend.codeastras.service.FileTreeService;
import com.codeastras.backend.codeastras.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/files")
@RequiredArgsConstructor
public class FileTreeController {
    private final FileTreeService fileTreeService;
    private final ProjectService projectService;

    @GetMapping("/tree")
    public ResponseEntity<List<FileNodeDto>> tree(@PathVariable UUID projectId, Authentication auth) {
        // Extract requester id from Authentication (your security places UUID as principal)
        UUID requesterId = (UUID) auth.getPrincipal();

        // 1) Validate permission (this will throw ForbiddenException if unauthorized)
        projectService.getProject(projectId, requesterId);

        // 2) Repair filesystem if missing (idempotent and safe)
        projectService.repairProjectFilesystemIfMissing(projectId);

        // 3) Now read the tree (service will also validate but we already checked)
        List<FileNodeDto> tree = fileTreeService.getFileTree(projectId, requesterId);
        return ResponseEntity.ok(tree);
    }
}
