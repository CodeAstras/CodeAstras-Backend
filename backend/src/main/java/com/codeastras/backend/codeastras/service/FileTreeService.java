package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.FileNodeDto;

import java.util.List;
import java.util.UUID;

public interface FileTreeService {
    List<FileNodeDto> getTree(UUID projectId, UUID userId);
}
