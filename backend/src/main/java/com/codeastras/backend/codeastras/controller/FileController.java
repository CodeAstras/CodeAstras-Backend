package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.ProjectFileContentDto;
import com.codeastras.backend.codeastras.dto.ProjectFileInfoDto;
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

    // -------------------------------
    // Get Entire File Tree
    // -------------------------------
    @GetMapping("/{projectId}/files")
    public ResponseEntity<List<ProjectFileInfoDto>> getProjectFiles(
            @PathVariable UUID projectId,
            Authentication auth
    ) {
        UUID userId = (UUID) auth.getPrincipal();
        List<ProjectFile> files = fileService.findAllByProjectId(projectId, userId);

        return ResponseEntity.ok(
                files.stream()
                        .map(ProjectFileInfoDto::from)
                        .toList()
        );
    }

    // -------------------------------
    // Load a Specific File
    // -------------------------------
    @GetMapping(path ="/{projectId}/file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProjectFileContentDto> getFile(
            @PathVariable UUID projectId,
            @RequestParam String path,
            Authentication auth
    ) {
        validatePath(path);

        UUID userId = (UUID) auth.getPrincipal();
        ProjectFile file = fileService.getFile(projectId, path, userId);

        return ResponseEntity.ok(ProjectFileContentDto.from(file));
    }

    // -------------------------------
    // Save File Content
    // -------------------------------
    @PutMapping("/{projectId}/file")
    public ResponseEntity<ProjectFileContentDto> saveFile(
            @PathVariable UUID projectId,
            @RequestParam String path,
            @RequestBody String content,
            Authentication auth
    ) throws IOException {

        validatePath(path);

        UUID userId = (UUID) auth.getPrincipal();
        ProjectFile updated = fileService.saveFileContent(projectId, path, content, userId);

        return ResponseEntity.ok(ProjectFileContentDto.from(updated));
    }

    // -------------------------------
    // Validation Helper
    // -------------------------------
    private void validatePath(String path) {
        if (path.startsWith("/") || path.startsWith("\\")) {
            throw new IllegalArgumentException("Path cannot start with slash");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        if (path.contains("..")) {
            throw new IllegalArgumentException("Invalid path");
        }
    }
}
