package com.codeastras.backend.codeastras.service.file;

import com.codeastras.backend.codeastras.service.PendingEdit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class DebouncedFileSaveManager {

    private static final Logger log =
            LoggerFactory.getLogger(DebouncedFileSaveManager.class);

    private static final long DEBOUNCE_MS = 500;

    private final FileService fileService;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "debounced-file-save");
                t.setDaemon(true);
                return t;
            });

    /**
     * key = projectId:path
     */
    private final Map<String, PendingEdit> pendingEdits = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();

    // DEBOUNCED SAVE
    public void scheduleSave(
            UUID projectId,
            String path,
            String content,
            UUID userId
    ) {
        if (projectId == null || path == null || path.isBlank()) return;

        // Normalize path ONCE
        String safePath = path.replace("\\", "/").replaceAll("/{2,}", "/");
        String key = projectId + ":" + safePath;

        pendingEdits.put(
                key,
                new PendingEdit(projectId, safePath, content, userId)
        );

        ScheduledFuture<?> existing = pendingTasks.remove(key);
        if (existing != null) {
            existing.cancel(false);
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                PendingEdit edit = pendingEdits.remove(key);
                if (edit != null) {
                    fileService.save(
                            edit.projectId(),
                            edit.path(),
                            edit.content(),
                            edit.userId()
                    );
                }
            } catch (Exception e) {
                log.error("Debounced save failed", e);
            } finally {
                pendingTasks.remove(key);
            }
        }, DEBOUNCE_MS, TimeUnit.MILLISECONDS);

        pendingTasks.put(key, future);
    }

    // HARD GUARANTEE BEFORE EXECUTION
    public void flushProject(UUID projectId) {

        log.info("Flushing pending edits for project {}", projectId);

        var keysToFlush = pendingEdits.keySet().stream()
                .filter(key -> key.startsWith(projectId.toString() + ":"))
                .toList();

        for (String key : keysToFlush) {

            PendingEdit edit = pendingEdits.remove(key);
            if (edit == null) continue;

            ScheduledFuture<?> task = pendingTasks.remove(key);
            if (task != null) task.cancel(false);

            try {
                fileService.save(
                        edit.projectId(),
                        edit.path(),
                        edit.content(),
                        edit.userId()
                );
                log.info("Flushed and saved: {}", edit.path());
            } catch (Exception e) {
                log.error("Flush failed for {}", key, e);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
