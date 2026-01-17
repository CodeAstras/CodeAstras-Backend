package com.codeastras.backend.codeastras.service.execution;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class RunRateLimiter {

    // Tune this safely
    private static final int MAX_RUNS_PER_MINUTE = 5;
    private static final long WINDOW_SECONDS = 60;

    // projectId -> timestamps
    private final Map<UUID, Deque<Instant>> runs = new ConcurrentHashMap<>();

    public boolean allow(UUID projectId) {
        Instant now = Instant.now();

        Deque<Instant> timestamps =
                runs.computeIfAbsent(projectId, k -> new ConcurrentLinkedDeque<>());

        synchronized (timestamps) {
            // remove old entries
            while (!timestamps.isEmpty() &&
                    timestamps.peekFirst()
                            .plusSeconds(WINDOW_SECONDS)
                            .isBefore(now)) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= MAX_RUNS_PER_MINUTE) {
                return false;
            }

            timestamps.addLast(now);
            return true;
        }
    }
}
