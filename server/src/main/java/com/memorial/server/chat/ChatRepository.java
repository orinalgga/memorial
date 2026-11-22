package com.memorial.server.chat;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findBySpaceOrderByCreatedAtDesc(String space, Pageable page);

    @Modifying
    @Query("delete from ChatMessage m where m.space = :space")
    int deleteBySpace(String space);
}
