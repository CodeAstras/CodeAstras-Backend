package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.FileNodeDto;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
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
    private final ProjectAccessManager accessManager;

    @GetMapping("/tree")
    public ResponseEntity<List<FileNodeDto>> tree(
            @PathVariable UUID projectId,
            Authentication auth
    ) {
        UUID userId = AuthUtil.requireUserId(auth);

        // 🔐 Single permission check
        accessManager.requireRead(projectId, userId);

        List<FileNodeDto> tree =
                fileTreeService.getFileTree(projectId, userId);

        return ResponseEntity.ok(tree);
    }
}
