package com.codeastras.backend.codeastras.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class DebouncedFileSaveManager {

    private static final long DEBOUNCE_MS = 500;

    private final FileService fileService;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private final Map<String, ScheduledFuture<?>> pending =
            new ConcurrentHashMap<>();

    public void scheduleSave(
            UUID projectId,
            String path,
            String content,
            UUID userId
    ) {
        String key = projectId + ":" + path;

        ScheduledFuture<?> existing = pending.get(key);
        if (existing != null) {
            existing.cancel(false);
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                fileService.saveFileContent(projectId, path, content, userId);
            } catch (Exception e) {
                // log only — do NOT throw
            } finally {
                pending.remove(key);
            }
        }, DEBOUNCE_MS, TimeUnit.MILLISECONDS);

        pending.put(key, future);
    }
}