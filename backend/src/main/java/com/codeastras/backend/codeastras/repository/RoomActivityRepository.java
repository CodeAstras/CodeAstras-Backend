package com.codeastras.backend.codeastras.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomActivityRepository extends JpaRepository<RoomActivity, UUID> {

}
