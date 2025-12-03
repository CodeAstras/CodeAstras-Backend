package com.codeastras.backend.codeastras.controller;


import com.codeastras.backend.codeastras.dto.*;
import com.codeastras.backend.codeastras.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

     private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    private UUID currentUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        UUID userId = currentUserId();
        RoomResponse roomResponse = roomService.createRoom(userId, request);
        return ResponseEntity.ok(roomResponse);
    }

    @PostMapping("/{roomid}/invite")
    public ResponseEntity<RoomResponse> invite(@PathVariable UUID roomId, @Valid @RequestBody InviteRequest inviteRequest) {
        UUID userId = currentUserId();
        roomService.inviteMember(userId, roomId, inviteRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable UUID roomId) {
        UUID userId = currentUserId();
        RoomResponse roomResponse = roomService.getRoom(userId, roomId);
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable UUID roomId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        UUID userId = currentUserId();
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(roomService.listMessages(userId, roomId, pageable));
    }

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<RoomMessageDto> postMessage(@PathVariable UUID roomId,
                                                      @RequestBody MessagePayload payload) {
        UUID userId = currentUserId();
        var response = roomService.postMessage(userId, roomId, payload.getContent(), payload.getType());
        return ResponseEntity.ok(response);
    }
}