package com.memorial.server.chat;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findBySpaceOrderByCreatedAtDesc(String space, Pageable page);
}
