package com.codeastras.backend.codeastras.presence;

import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class PresenceCleanupTask {

    private final SessionRegistry sessionRegistry;

    @Scheduled(fixedDelay = 10_000)
    public void cleanup() {
        sessionRegistry.cleanupStalePresence();
    }
}
