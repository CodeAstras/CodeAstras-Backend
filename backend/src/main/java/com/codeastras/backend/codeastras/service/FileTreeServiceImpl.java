package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.config.StorageProperties;
import com.codeastras.backend.codeastras.dto.FileNodeDto;
import com.codeastras.backend.codeastras.dto.NodeType;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileTreeServiceImpl implements FileTreeService {

    private final ProjectRepository projectRepo;
    private final PermissionService permissionService;
    private final StorageProperties storageProperties;

    private static final int MAX_DEPTH = 10;

    @Override
    public List<FileNodeDto> getFileTree(UUID projectId, UUID userId) {

        permissionService.checkProjectReadAccess(projectId, userId);

        Path projectRoot = resolveProjectRoot(projectId);

        if (!Files.exists(projectRoot)) {
            throw new ResourceNotFoundException("Missing folder for project " + projectId);
        }
        if (!Files.isDirectory(projectRoot)) {
            throw new ResourceNotFoundException("Project root is not a directory");
        }

        try {
            return scanDirectory(projectRoot);
        } catch (IOException e) {
            throw new RuntimeException("File tree scan failed", e);
        }
    }

    private Path resolveProjectRoot(UUID projectId) {
        return Paths.get(storageProperties.getProjects())
                .resolve(projectId.toString())
                .normalize();
    }


    private List<FileNodeDto> scanDirectory(Path root) throws IOException {

        FileNodeDto rootNode = new FileNodeDto(
                root.getFileName().toString(),
                "",
                NodeType.FOLDER,
                null,
                Files.getLastModifiedTime(root).toInstant(),
                new ArrayList<>()
        );

        Deque<FileNodeDto> stack = new ArrayDeque<>();
        stack.push(rootNode);

        Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), MAX_DEPTH,
                new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

                        if (dir.equals(root)) {
                            return FileVisitResult.CONTINUE;
                        }

                        String rel = normalize(root.relativize(dir).toString());

                        FileNodeDto node = new FileNodeDto(
                                dir.getFileName().toString(),
                                rel,
                                NodeType.FOLDER,
                                null,
                                attrs.lastModifiedTime().toInstant(),
                                new ArrayList<>()
                        );

                        stack.peek().getChildren().add(node);
                        stack.push(node);

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                        String rel = normalize(root.relativize(file).toString());

                        FileNodeDto node = new FileNodeDto(
                                file.getFileName().toString(),
                                rel,
                                NodeType.FILE,
                                attrs.size(),
                                attrs.lastModifiedTime().toInstant(),
                                null
                        );

                        stack.peek().getChildren().add(node);

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        if (!dir.equals(root)) {
                            stack.pop();
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    private String normalize(String p) {
                        return p.replace("\\", "/");
                    }
                });

        return rootNode.getChildren(); // children of the root folder
    }

}
