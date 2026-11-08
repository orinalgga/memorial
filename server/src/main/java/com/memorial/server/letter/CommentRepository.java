package com.memorial.server.letter;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, String> {

    List<Comment> findByLetterIdOrderByCreatedAtAsc(String letterId, Pageable page);
}
