package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.FileNodeDto;
import com.codeastras.backend.codeastras.dto.NodeType;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileTreeServiceImpl implements FileTreeService {

    private final ProjectFileRepository projectFileRepository;
    private final ProjectAccessManager accessManager;
    private final FileSyncService fileSyncService;

    @Override
    public List<FileNodeDto> getTree(UUID projectId, UUID userId) {

        // 🔐 SINGLE AUTHORITY
        accessManager.require(projectId, userId, ProjectPermission.READ_TREE);

        List<ProjectFile> files =
                projectFileRepository.findByProjectId(projectId);

        // Use LinkedHashMap for deterministic order
        Map<String, FileNodeDto> nodeMap = new LinkedHashMap<>();
        List<FileNodeDto> roots = new ArrayList<>();

        for (ProjectFile file : files) {

            // 🔒 Canonical path
            String normalizedPath =
                    fileSyncService.sanitizeUserPath(file.getPath());

            String[] parts = normalizedPath.split("/");

            StringBuilder currentPath = new StringBuilder();

            for (int i = 0; i < parts.length; i++) {
                if (i > 0) currentPath.append("/");
                currentPath.append(parts[i]);

                String key = currentPath.toString();

                if (!nodeMap.containsKey(key)) {

                    boolean isFile =
                            i == parts.length - 1 &&
                                    "FILE".equalsIgnoreCase(file.getType());

                    FileNodeDto node = new FileNodeDto(
                            parts[i],
                            key,
                            isFile ? NodeType.FILE : NodeType.FOLDER,
                            isFile
                                    ? (long) (file.getContent() == null
                                    ? 0
                                    : file.getContent().length())
                                    : null,
                            // folders get stable timestamps
                            isFile
                                    ? (file.getUpdatedAt() != null
                                    ? file.getUpdatedAt()
                                    : Instant.now())
                                    : Instant.EPOCH,
                            isFile ? null : new ArrayList<>()
                    );

                    nodeMap.put(key, node);

                    if (i == 0) {
                        roots.add(node);
                    } else {
                        String parentKey =
                                key.substring(0, key.lastIndexOf("/"));
                        FileNodeDto parent = nodeMap.get(parentKey);
                        parent.getChildren().add(node);
                    }
                }
            }
        }

        // 🔽 Optional: sort children alphabetically
        roots.forEach(this::sortRecursively);

        return roots;
    }

    private void sortRecursively(FileNodeDto node) {
        if (node.getChildren() == null) return;

        node.getChildren().sort(Comparator.comparing(FileNodeDto::getName));

        for (FileNodeDto child : node.getChildren()) {
            sortRecursively(child);
        }
    }
}
