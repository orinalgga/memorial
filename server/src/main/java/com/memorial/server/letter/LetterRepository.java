package com.memorial.server.letter;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LetterRepository extends JpaRepository<Letter, String> {

    List<Letter> findBySpaceOrderByCreatedAtDesc(String space, Pageable page);

    @Modifying
    @Query("update Letter l set l.hearts = l.hearts + 1 where l.id = :id")
    void addHeart(String id);

    @Modifying
    @Query("update Letter l set l.commentCount = l.commentCount + 1 where l.id = :id")
    void addComment(String id);
}
