package com.codeastras.backend.codeastras.entity.collaborator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CollaborationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CollaborationEventPublisher.class);

    public void publish(
            CollaborationEventType type,
            UUID projectId,
            UUID userId
    ) {
        // Step 6: log only
        // Step 7: WebSocket
        // Step 8: Kafka / Redis if needed
        log.info("Event {} project={} user={}", type, projectId, userId);
    }
}

