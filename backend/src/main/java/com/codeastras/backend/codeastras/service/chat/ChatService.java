package com.codeastras.backend.codeastras.service.chat;

import com.codeastras.backend.codeastras.entity.chat.ChatMessage;
import com.codeastras.backend.codeastras.entity.chat.MessageType;
import com.codeastras.backend.codeastras.repository.chat.ChatRepository;
import com.codeastras.backend.codeastras.websocket.chat.ChatRoomRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRegistry roomRegistry;

    public void processUserMessage(UUID projectId, UUID userId, String username, String content) {
        // 1. Persist
        ChatMessage message = ChatMessage.builder()
                .projectId(projectId)
                .senderId(userId)
                .senderName(username)
                .content(content)
                .type(MessageType.USER)
                .build();
        chatRepository.save(message);

        // 2. Broadcast
        roomRegistry.broadcast(projectId, message);
    }

    public void systemMessage(UUID projectId, String content) {
        // 1. Persist
        ChatMessage message = ChatMessage.builder()
                .projectId(projectId)
                .senderId(null)
                .senderName("System")
                .content(content)
                .type(MessageType.SYSTEM)
                .build();
        chatRepository.save(message);

        // 2. Broadcast
        roomRegistry.broadcast(projectId, message);
    }

    public List<ChatMessage> getRecentMessages(UUID projectId) {
        // Get last 50 messages
        return chatRepository.findLatestByProject(projectId, PageRequest.of(0, 50));
    }
}
