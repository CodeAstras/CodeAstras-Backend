package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CreateRoomRequest;
import com.codeastras.backend.codeastras.dto.InviteRequest;
import com.codeastras.backend.codeastras.dto.RoomMessageDto;
import com.codeastras.backend.codeastras.dto.RoomResponse;
import com.codeastras.backend.codeastras.entity.*;
import com.codeastras.backend.codeastras.events.*;
import com.codeastras.backend.codeastras.exception.*;
import com.codeastras.backend.codeastras.mapper.RoomMapper;
import com.codeastras.backend.codeastras.repository.RoomMemberRepository;
import com.codeastras.backend.codeastras.repository.RoomMessageRepository;
import com.codeastras.backend.codeastras.repository.RoomRepository;
import com.codeastras.backend.codeastras.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService {

    private final Logger log = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomMessageRepository roomMessageRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RoomServiceImpl(RoomRepository roomRepository,
                           RoomMemberRepository roomMemberRepository,
                           RoomMessageRepository roomMessageRepository,
                           UserRepository userRepository,
                           ApplicationEventPublisher eventPublisher) {

        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.roomMessageRepository = roomMessageRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    // -----------------------------
    // CREATE ROOM
    // -----------------------------
    @Override
    @Transactional
    public RoomResponse createRoom(UUID creatorUserId, CreateRoomRequest request) {

        var creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found"));

        Room room = new Room();
        room.setId(UUID.randomUUID());
        room.setName(request.getName());
        room.setCreatedBy(creator);
        room.setCreatedAt(Timestamp.from(Instant.now()));
        room.setActive(true);

        // Save room and create final reference for lambda
        room = roomRepository.save(room);
        final Room finalRoom = room;

        // Add creator as ADMIN
        RoomMember admin = new RoomMember();
        admin.setId(UUID.randomUUID());
        admin.setRoom(finalRoom);
        admin.setUser(creator);
        admin.setRole(RoomRole.ADMIN);
        admin.setJoinedAt(Timestamp.from(Instant.now()));
        roomMemberRepository.save(admin);

        // Auto-invite emails
        if (request.getInviteEmails() != null) {

            for (String email : request.getInviteEmails()) {

                final String finalEmail = email;

                userRepository.findByEmail(finalEmail).ifPresentOrElse(invUser -> {
                    try {
                        addMember(creatorUserId, finalRoom.getId(), invUser.getId());
                    } catch (Exception ex) {
                        log.warn("Could not auto-add member {} → {}", finalEmail, ex.getMessage());
                    }
                }, () -> {
                    eventPublisher.publishEvent(
                            new RoomInviteEvent(this, finalRoom.getId(), finalEmail, creatorUserId)
                    );
                });
            }
        }

        // Fire room-created event
        eventPublisher.publishEvent(
                new RoomCreatedEvent(this, room.getId(), creatorUserId)
        );


        // Ensure members list contains admin for response
        finalRoom.getMembers().add(admin);

        return RoomMapper.toResponse(finalRoom);
    }


    // -----------------------------
    // GET ROOM DETAILS
    // -----------------------------
    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoom(UUID requesterUserId, UUID roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found: " + roomId));

        boolean isMember = roomMemberRepository.existsByRoomIdAndUserId(roomId, requesterUserId);

        if (!isMember && !room.getCreatedBy().getId().equals(requesterUserId)) {
            throw new UnauthorizedRoomAccessException("You are not part of this room");
        }

        var members = roomMemberRepository.findByRoom(room);
        room.setMembers(members);

        return RoomMapper.toResponse(room);
    }

    // -----------------------------
    // INVITE MEMBER
    // -----------------------------
    @Override
    @Transactional
    public void inviteMember(UUID requesterUserId, UUID roomId, InviteRequest inviteRequest) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        var requesterMembership = roomMemberRepository.findByRoomIdAndUserId(roomId, requesterUserId)
                .orElseThrow(() -> new UnauthorizedRoomAccessException("Not a member"));

        // REAL permission logic
        boolean requesterIsCreator = room.getCreatedBy().getId().equals(requesterUserId);
        boolean requesterIsAdmin = requesterMembership.getRole() == RoomRole.ADMIN;

        if (!requesterIsCreator && !requesterIsAdmin) {
            throw new UnauthorizedRoomAccessException("Only admins or the room creator can invite");
        }

        var userOpt = userRepository.findByEmail(inviteRequest.getEmail());

        if (userOpt.isPresent()) {
            var invitee = userOpt.get();

            if (roomMemberRepository.existsByRoomIdAndUserId(roomId, invitee.getId())) {
                throw new DuplicateMemberException("User already a room member");
            }

            addMember(requesterUserId, roomId, invitee.getId());

        } else {
            eventPublisher.publishEvent(new RoomInviteEvent(this, roomId,
                    inviteRequest.getEmail(), requesterUserId));
        }
    }

    // -----------------------------
    // ADD MEMBER
    // -----------------------------
    @Override
    @Transactional
    public void addMember(UUID actorUserId, UUID roomId, UUID userId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        if (roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new DuplicateMemberException("User already a member");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        RoomMember member = new RoomMember();
        member.setId(UUID.randomUUID());
        member.setRoom(room);
        member.setUser(user);
        member.setRole(RoomRole.MEMBER);
        member.setJoinedAt(Timestamp.from(Instant.now()));

        roomMemberRepository.save(member);

        eventPublisher.publishEvent(
                new RoomMemberAddedEvent(
                        this,           // source
                        roomId,         // roomId
                        userId,         // newMemberId
                        actorUserId     // addedBy
                )
        );
    }

    // -----------------------------
    // REMOVE MEMBER
    // -----------------------------
    @Override
    @Transactional
    public void removeMember(UUID actorUserId, UUID roomId, UUID userId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        var memberOpt = roomMemberRepository.findByRoomIdAndUserId(roomId, userId);
        if (memberOpt.isEmpty()) {
            throw new ResourceNotFoundException("Member not found");
        }

        var member = memberOpt.get();

        var actorMembership = roomMemberRepository.findByRoomIdAndUserId(roomId, actorUserId)
                .orElseThrow(() -> new UnauthorizedRoomAccessException("You are not a room member"));

        boolean actorIsCreator = room.getCreatedBy().getId().equals(actorUserId);
        boolean actorIsAdmin = actorMembership.getRole() == RoomRole.ADMIN;
        boolean removingSelf = actorUserId.equals(userId);

        if (!actorIsCreator && !actorIsAdmin && !removingSelf) {
            throw new UnauthorizedRoomAccessException("Not authorized to remove this member");
        }

        roomMemberRepository.delete(member);

        eventPublisher.publishEvent(
                new RoomMemberAddedEvent(this, roomId, userId, actorUserId)
        );

    }

    // -----------------------------
    // LIST MESSAGES
    // -----------------------------
    @Override
    @Transactional(readOnly = true)
    public Page<RoomMessageDto> listMessages(UUID requesterId, UUID roomId, Pageable pageable) {

        if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, requesterId)) {
            throw new UnauthorizedRoomAccessException("Not a member");
        }

        var messages = roomMessageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable);

        return messages.map(RoomMapper::toMessageDto);
    }

    // -----------------------------
    // POST MESSAGE
    // -----------------------------
    @Override
    @Transactional
    public RoomMessageDto postMessage(UUID userId, UUID roomId, String content, String type) {

        if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new UnauthorizedRoomAccessException("Not a member");
        }

        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RoomMessage message = new RoomMessage();
        message.setId(UUID.randomUUID());
        message.setRoom(room);
        message.setUser(user);
        message.setContent(content);
        message.setType(MessageType.valueOf(type));
        message.setTimestamp(Timestamp.from(Instant.now()));

        message = roomMessageRepository.save(message);

        RoomMessageDto dto = RoomMapper.toMessageDto(message);

        eventPublisher.publishEvent(new RoomMessageEvent(this, dto));

        return dto;
    }
}
