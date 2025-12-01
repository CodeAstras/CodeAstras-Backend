package com.codeastras.backend.codeastras.events;

import com.codeastras.backend.codeastras.service.RoomServiceImpl;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class RoomInviteEvent extends ApplicationEvent {

    private final UUID roomId;
    private final String inviteEmail;
    private final UUID inviterId;

    public RoomInviteEvent(Object source, UUID roomId, String inviteEmail, UUID inviterId) {
        super(source);
        this.roomId = roomId;
        this.inviteEmail = inviteEmail;
        this.inviterId = inviterId;
    }

    public UUID getRoomId() { return roomId; }
    public String getInviteEmail() { return inviteEmail; }
    public UUID getInviterId() { return inviterId; }
}