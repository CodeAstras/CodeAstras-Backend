package com.codeastras.backend.codeastras.controller.chat;

import com.codeastras.backend.codeastras.entity.chat.MessageType;
import com.codeastras.backend.codeastras.dto.chat.CallSignalMessage;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.chat.CallService;
import com.codeastras.backend.codeastras.store.CallSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CallDisconnectListener {

    private final CallSessionRegistry registry;
    private final CallService callService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if(principal == null) return;

        UUID userId = AuthUtil.requireUserId(principal);
        UUID projectId = registry.getProjectForUser(userId);

        if (projectId == null) return;

        callService.leaveCall(projectId, userId);

        // notify others
        CallSignalMessage leaveMsg = new CallSignalMessage();
        leaveMsg.setType(MessageType.CALL_LEAVE);
        leaveMsg.setProjectId(projectId);
        leaveMsg.setFromUserId(userId);

        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId + "/call",
                leaveMsg
        );
    }
}
