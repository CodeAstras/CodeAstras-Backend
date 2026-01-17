package com.codeastras.backend.codeastras.service.file;

import com.codeastras.backend.codeastras.dto.file.FileNodeDto;

import java.util.List;
import java.util.UUID;

public interface FileTreeService {
    List<FileNodeDto> getTree(UUID projectId, UUID userId);
}
