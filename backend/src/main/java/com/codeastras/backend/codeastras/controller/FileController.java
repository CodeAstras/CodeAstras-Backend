package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{projectId}/files")
    public List<ProjectFile> getProjectFiles(@PathVariable UUID projectId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return fileService.findAllByProjectId(projectId, userId);
    }

    @GetMapping(path ="/{projectId}/file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectFile> getFile (
            @PathVariable UUID projectId,
            @RequestParam String path,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        ProjectFile file = fileService.getFile(projectId, path, userId);
        return ResponseEntity.ok(file);
    }

    @PutMapping("/{projectId}/file")
    public ResponseEntity<ProjectFile> saveFile(
            @PathVariable UUID projectId,
            @RequestParam String path,
            @RequestBody String content,
            Authentication authentication
    ) throws IOException {
        UUID userId = (UUID) authentication.getPrincipal();
        ProjectFile updated = fileService.saveFileContent(projectId, path, content, userId);
        return ResponseEntity.ok(updated);
    }
}
