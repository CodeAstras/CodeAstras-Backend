package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CreateRoomRequest;
import com.codeastras.backend.codeastras.dto.InviteRequest;
import com.codeastras.backend.codeastras.dto.RoomMessageDto;
import com.codeastras.backend.codeastras.dto.RoomResponse;
import com.codeastras.backend.codeastras.entity.RoomMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public interface RoomService {
    RoomResponse createRoom(UUID creatorUserId, CreateRoomRequest request);
    RoomResponse getRoom(UUID requestUserId, UUID roomId);
    void inviteMember(UUID requestUserId, UUID roomId, InviteRequest inviteRequest);
    void addMember(UUID actorUserId, UUID roomId, UUID userId);
    void removeMember(UUID actorUserId, UUID roomId, UUID userId);

    @Transactional(readOnly = true)
    Page<RoomMessageDto> listMessages(UUID requesterUserId, UUID roomId, Pageable pageable);


    RoomMessageDto postMessage(UUID userId, UUID roomId, String content, String type);

}
