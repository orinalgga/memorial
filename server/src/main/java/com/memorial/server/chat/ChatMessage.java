package com.memorial.server.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    private String id;

    @Column(nullable = false)
    private String space;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "visitor_id")
    private String visitorId;

    @Column(nullable = false)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ChatMessage() {}

    public ChatMessage(String id, String space, String authorName, String visitorId, String text, Instant createdAt) {
        this.id = id;
        this.space = space;
        this.authorName = authorName;
        this.visitorId = visitorId;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getAuthorName() { return authorName; }
    public String getVisitorId() { return visitorId; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
}
