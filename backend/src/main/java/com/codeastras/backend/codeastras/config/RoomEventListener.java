package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.dto.RoomMessageDto;
import com.codeastras.backend.codeastras.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RoomEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public RoomEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleRoomMessage(RoomMessageEvent evt) {
        // publish to topic: /topic/rooms/{roomId}/messages
        RoomMessageDto message = evt.getMessage();
        messagingTemplate.convertAndSend("/topic/rooms/" + message.getRoomId() + "/messages", message);
    }

    @EventListener
    public void handleMemberAdded(RoomMemberAddedEvent evt) {
        messagingTemplate.convertAndSend("/topic/rooms/" + evt.getRoomId() + "/members",
                (Object) Map.of("event", "member_added", "userId", evt.getNewMemberId(), "addedBy", evt.getAddedBy()));
    }

    @EventListener
    public void handleMemberRemoved(RoomMemberRemovedEvent evt) {
        messagingTemplate.convertAndSend("/topic/rooms/" + evt.getRoomId() + "/members",
                (Object) Map.of("event", "member_removed", "userId", evt.getUserId(), "removedBy", evt.getRemovedBy()));
    }

    @EventListener
    public void handleRoomInvite(RoomInviteEvent evt) {
        // optional: notify inviter that invite was created
        messagingTemplate.convertAndSendToUser(evt.getInviteEmail(),
                "/queue/invites", Map.of("roomId", evt.getRoomId(), "invitedBy", evt.getInviterId()));
    }

    @EventListener
    public void handleRoomCreated(RoomCreatedEvent evt) {
        messagingTemplate.convertAndSend("/topic/users/" + evt.getCreatorId() + "/rooms",
                (Object) Map.of("event", "room_created", "roomId", evt.getRoomId()));
    }
}