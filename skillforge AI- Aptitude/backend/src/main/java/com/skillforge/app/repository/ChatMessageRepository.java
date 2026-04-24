package com.skillforge.app.repository;

import com.skillforge.app.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(Long userId);
}
