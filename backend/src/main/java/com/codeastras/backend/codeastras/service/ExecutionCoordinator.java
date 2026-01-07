package com.codeastras.backend.codeastras.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExecutionCoordinator {

    private final DebouncedFileSaveManager debouncedFileSaveManager;
    private final FileSyncService fileSyncService;

    /**
     * Hard guarantee:
     * Editor → DB → FS is consistent before execution
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void flushBeforeExecution(UUID projectId) {
        debouncedFileSaveManager.flushProject(projectId);
    }
}
