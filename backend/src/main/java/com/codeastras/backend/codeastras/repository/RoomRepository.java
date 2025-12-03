package com.codeastras.backend.codeastras.repository;

import com.codeastras.backend.codeastras.entity.Room;
import com.codeastras.backend.codeastras.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
}
