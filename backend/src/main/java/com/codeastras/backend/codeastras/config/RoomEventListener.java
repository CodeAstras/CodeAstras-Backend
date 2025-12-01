package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RoomEventListener {

    @EventListener
    public void onRoomCreated(RoomCreatedEvent event) {
        // TODO: broadcast room created to user dashboard or activity feed via WebSocket
    }

    @EventListener
    public void onRoomInvite(RoomInviteEvent event) {
        // TODO: trigger email invite or WebSocket notification if invited user is online
    }

    @EventListener
    public void onMemberAdded(RoomMemberAddedEvent event) {
        // TODO: broadcast member joined to room WebSocket channel
    }

    @EventListener
    public void onMemberRemoved(RoomMemberRemovedEvent event) {
        // TODO: broadcast member left to room WebSocket channel
    }

    @EventListener
    public void onMessage(RoomMessageEvent event) {
        // TODO: broadcast chat message to room WebSocket
    }
}