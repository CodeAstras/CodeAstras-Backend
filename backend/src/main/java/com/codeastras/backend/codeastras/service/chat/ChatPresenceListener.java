package com.codeastras.backend.codeastras.service.chat;

import com.codeastras.backend.codeastras.dto.presence.PresenceEvent;
import com.codeastras.backend.codeastras.dto.presence.PresenceEventType;
import com.codeastras.backend.codeastras.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatPresenceListener {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void onPresenceEvent(PresenceEvent event) {
        // We only care about JOIN/LEFT for now to create system messages
        if (event.getType() == PresenceEventType.USER_JOINED) {
            String username = getUsername(event.getUserId());
            chatService.systemMessage(event.getProjectId(), username + " joined the workspace.");
        } else if (event.getType() == PresenceEventType.USER_LEFT) {
            String username = getUsername(event.getUserId());
            chatService.systemMessage(event.getProjectId(), username + " left the workspace.");
        }
    }

    private String getUsername(java.util.UUID userId) {
        return userRepository.findById(userId)
                .map(u -> u.getUsername())
                .orElse("Unknown User");
    }
}
