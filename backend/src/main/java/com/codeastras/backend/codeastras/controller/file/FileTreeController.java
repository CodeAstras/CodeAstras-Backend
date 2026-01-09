package com.codeastras.backend.codeastras.controller.file;

import com.codeastras.backend.codeastras.dto.file.FileNodeDto;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.file.FileTreeService;
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

    @GetMapping("/tree")
    public ResponseEntity<List<FileNodeDto>> tree(
            @PathVariable UUID projectId,
            Authentication auth
    ) {
        UUID userId = AuthUtil.requireUserId(auth);
        return ResponseEntity.ok(
                fileTreeService.getTree(projectId, userId)
        );
    }
}
