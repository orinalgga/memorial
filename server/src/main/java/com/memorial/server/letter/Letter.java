package com.memorial.server.letter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 사연(공용 공간) 또는 편지(개인 추모 공간). 화면에서는 등불로 떠다닌다. */
@Entity
@Table(name = "letters")
public class Letter {

    @Id
    private String id;

    @Column(nullable = false)
    private String space;

    @Column(name = "to_name", nullable = false)
    private String toName;

    @Column(nullable = false)
    private String message;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "visitor_id")
    private String visitorId;

    @Column(nullable = false)
    private int hearts;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Letter() {}

    public Letter(String id, String space, String toName, String message, String authorName, String visitorId, Instant createdAt) {
        this.id = id;
        this.space = space;
        this.toName = toName;
        this.message = message;
        this.authorName = authorName;
        this.visitorId = visitorId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getSpace() { return space; }
    public String getToName() { return toName; }
    public String getMessage() { return message; }
    public String getAuthorName() { return authorName; }
    public int getHearts() { return hearts; }
    public int getCommentCount() { return commentCount; }
    public Instant getCreatedAt() { return createdAt; }
}
