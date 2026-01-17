package com.codeastras.backend.codeastras.controller.file;

import com.codeastras.backend.codeastras.dto.file.CreateFileRequest;
import com.codeastras.backend.codeastras.dto.project.ProjectFileContentDto;
import com.codeastras.backend.codeastras.dto.project.ProjectFileInfoDto;
import com.codeastras.backend.codeastras.entity.file.ProjectFile;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.file.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // ------------------------------------------------
    // Get all files (flat list, DB-based)
    // ------------------------------------------------
    @GetMapping("/{projectId}/files")
    public ResponseEntity<List<ProjectFileInfoDto>> getProjectFiles(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        UUID userId = AuthUtil.requireUserId(authentication);

        List<ProjectFile> files =
                fileService.findAll(projectId, userId);

        return ResponseEntity.ok(
                files.stream()
                        .map(ProjectFileInfoDto::from)
                        .toList()
        );
    }

    // ------------------------------------------------
    // Get single file (content)
    // ------------------------------------------------
    @GetMapping(
            path = "/{projectId}/file",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProjectFileContentDto> getFile(
            @PathVariable UUID projectId,
            @RequestParam String path,
            Authentication authentication
    ) {
        UUID userId = AuthUtil.requireUserId(authentication);

        ProjectFile file =
                fileService.getFile(projectId, path, userId);

        return ResponseEntity.ok(ProjectFileContentDto.from(file));
    }

    // ------------------------------------------------
    // Save file content
    // ------------------------------------------------
    @PutMapping("/{projectId}/file")
    public ResponseEntity<ProjectFileContentDto> saveFile(
            @PathVariable UUID projectId,
            @RequestParam String path,
            @RequestBody String content,
            Authentication authentication
    ) throws IOException {

        UUID userId = AuthUtil.requireUserId(authentication);

        ProjectFile updated =
                fileService.save(projectId, path, content, userId);

        return ResponseEntity.ok(ProjectFileContentDto.from(updated));
    }

    // ------------------------------------------------
    // Create new file / folder
    // ------------------------------------------------
    @PostMapping("/{projectId}/files")
    public ResponseEntity<ProjectFileInfoDto> createFile(
            @PathVariable UUID projectId,
            @RequestBody CreateFileRequest body,
            Authentication authentication
    ) throws IOException {

        UUID userId = AuthUtil.requireUserId(authentication);

        ProjectFile file = fileService.createFile(
                projectId,
                body.getPath(),
                body.getType(),
                userId
        );

        return ResponseEntity.ok(ProjectFileInfoDto.from(file));
    }
}
