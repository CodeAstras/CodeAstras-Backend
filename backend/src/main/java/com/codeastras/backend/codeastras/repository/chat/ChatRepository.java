package com.codeastras.backend.codeastras.repository.chat;

import com.codeastras.backend.codeastras.entity.chat.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m WHERE m.projectId = :projectId ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestByProject(@Param("projectId") UUID projectId, Pageable pageable);
}
