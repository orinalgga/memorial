package com.memorial.server.letter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 사연에 단 댓글. parentId가 있으면 그 댓글의 답글이다(한 단계까지). */
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    private String id;

    @Column(name = "letter_id", nullable = false)
    private String letterId;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "visitor_id")
    private String visitorId;

    @Column(nullable = false)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Comment() {}

    public Comment(String id, String letterId, String parentId, String authorName, String visitorId, String text, Instant createdAt) {
        this.id = id;
        this.letterId = letterId;
        this.parentId = parentId;
        this.authorName = authorName;
        this.visitorId = visitorId;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getLetterId() { return letterId; }
    public String getParentId() { return parentId; }
    public String getAuthorName() { return authorName; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
}
