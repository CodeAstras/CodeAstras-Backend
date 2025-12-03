package com.codeastras.backend.codeastras.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(
        name = "room_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"})
)
public class RoomMember {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomRole role = RoomRole.MEMBER;

    private Timestamp joinedAt = new Timestamp(System.currentTimeMillis());

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RoomRole getRole() {
        return role;
    }

    public void setRole(RoomRole role) {
        this.role = role;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }
}

