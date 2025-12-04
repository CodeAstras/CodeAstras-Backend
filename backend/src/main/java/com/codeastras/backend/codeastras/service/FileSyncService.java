package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class FileSyncService {

    private final Logger log = LoggerFactory.getLogger(FileSyncService.class);

    private final ProjectFileRepository fileRepo;

    private final Path basePath;

    public FileSyncService(ProjectFileRepository fileRepo,
                           @Value("${code.runner.base-path:/var/code_sessions}") String basePath) {
        this.fileRepo = fileRepo;
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    }

    public void syncProjectToSession(UUID projectId, String sessionId) throws IOException {
        Path sessionDir = getSessionDir(sessionId);

        log.info("Syncing project {} -> to session {}", projectId, sessionDir);
        Files.createDirectories(sessionDir);

        List<ProjectFile> files = fileRepo.findByProjectId(projectId);

        for(ProjectFile f : files) {
            if("FOLDER".equalsIgnoreCase(f.getType())) {
                Path dir = resolvePathSafely(sessionDir, f.getPath());
                Files.createDirectories(dir);
            } else {
                Path file = resolvePathSafely(sessionDir, f.getPath());
                if(file.getParent() != null) {
                    Files.createDirectories(file.getParent());
                }
                String content = f.getContent() == null ? "" : f.getContent();
                Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        }
    }

    public void writeFileToSession(String sessionId, String path, String content) throws IOException {
        Path sessionDir = getSessionDir(sessionId);
        Path filePath = resolvePathSafely(sessionDir, path);

        if(filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }
        Files.writeString(filePath, content == null ? "" : content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("Wrote file {} to session {}", filePath, sessionId);
    }

    public void removeSessionFolder(String sessionId) throws IOException {
        Path sessionDir = getSessionDir(sessionId);
        if(Files.exists(sessionDir)) {
            log.info("Deleting session folder {}", sessionDir);

            Files.walk(sessionDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            log.warn("Failed to delete file {}", p);
                        }
                    });
        }
    }

    /** helper: session dir path */
    private Path getSessionDir(String sessionId) {
        return basePath.resolve(sessionId).toAbsolutePath().normalize();
    }

    /**
     * Resolve a user-provided path safely under the session directory.
     * Prevents path traversal outside sessionDir.
     */
    private Path resolvePathSafely(Path sessionDir, String userPath) {
        // normalize userPath
        String cleaned = userPath == null ? "main.py" : userPath;

        if (userPath.contains("..") || userPath.contains("\\")) {
            throw new IllegalArgumentException("Invalid path");
        }

        // remove leading slashes
        if (cleaned.startsWith("/")) cleaned = cleaned.substring(1);
        // prevent .. segments
        Path target = sessionDir.resolve(cleaned).normalize();
        if (!target.startsWith(sessionDir)) {
            throw new IllegalArgumentException("Invalid path (path traversal detected): " + userPath);
        }
        return target;
    }
}
