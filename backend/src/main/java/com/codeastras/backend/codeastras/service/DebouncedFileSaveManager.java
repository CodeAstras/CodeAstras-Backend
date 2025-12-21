package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
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
    private final ProjectFileRepository fileRepo;

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


    public void scheduleSave(UUID projectId, String path, String content, UUID userId) {
        if (projectId == null || path == null || path.isBlank()) return;

        String key = projectId + ":" + path;

        pendingEdits.put(key, new PendingEdit(projectId, path, content, userId));

        ScheduledFuture<?> existing = pendingTasks.remove(key);
        if (existing != null) existing.cancel(false);

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


    /**
     * 🔥 HARD GUARANTEE:
     * Flush ALL pending edits for a project synchronously.
     * Called before execution snapshot.
     */
    public void flushProject(UUID projectId) {
        log.info("🚀 FORCING DATABASE UPDATE for project {}", projectId);

        java.util.List<String> keysToFlush = pendingEdits.keySet().stream()
                .filter(key -> key.startsWith(projectId.toString() + ":"))
                .toList();

        for (String key : keysToFlush) {
            PendingEdit edit = pendingEdits.remove(key);
            if (edit == null) continue;

            pendingTasks.remove(key);

            try {
                // 1. Force SQL Update immediately via direct query
                fileRepo.updateContent(edit.projectId(), edit.path(), edit.content());

                // 2. Update the physical project file on disk
                fileService.save(edit.projectId(), edit.path(), edit.content(), edit.userId());

                log.info("✅ Flushed and saved: {}", edit.path());
            } catch (Exception e) {
                log.error("Flush failed for {}", key, e);
            }
        }
    }


    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
