package com.codeastras.backend.codeastras.mapper;

import com.codeastras.backend.codeastras.dto.RoomMemberDto;
import com.codeastras.backend.codeastras.dto.RoomMessageDto;
import com.codeastras.backend.codeastras.dto.RoomResponse;
import com.codeastras.backend.codeastras.entity.Room;
import com.codeastras.backend.codeastras.entity.RoomMember;
import com.codeastras.backend.codeastras.entity.RoomMessage;

import java.util.stream.Collectors;

public class RoomMapper {

    public static RoomResponse toResponse(Room room) {
        RoomResponse dto = new RoomResponse();

        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCreatedBy(room.getCreatedBy().getId());
        dto.setCreatedAt(room.getCreatedAt().toInstant());
        dto.setIsActive(room.isActive());

        // null safety
        if (room.getMembers() != null) {
            dto.setMembers(
                    room.getMembers().stream().map(RoomMapper::toMemberDto).collect(Collectors.toList())
            );
        }

        return dto;
    }

    private static RoomMemberDto toMemberDto(RoomMember member) {
        RoomMemberDto dto = new RoomMemberDto();
        dto.setUserId(member.getUser().getId());
        dto.setRole(member.getRole().name());
        dto.setJoinedAt(member.getJoinedAt().toInstant());
        return dto;
    }

    public static RoomMessageDto toMessageDto(RoomMessage message) {
        RoomMessageDto dto = new RoomMessageDto();

        dto.setId(message.getId());
        dto.setRoomId(message.getRoom().getId());

        dto.setUserId(message.getUser() != null ? message.getUser().getId() : null);

        dto.setContent(message.getContent());
        dto.setType(message.getType().name());
        dto.setTimestamp(message.getTimestamp().toInstant());

        return dto;
    }
}
