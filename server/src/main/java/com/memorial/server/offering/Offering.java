package com.memorial.server.offering;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 제단에 올린 촛불(candle)과 국화(flower). */
@Entity
@Table(name = "offerings")
public class Offering {

    @Id
    private String id;

    @Column(nullable = false)
    private String space;

    @Column(nullable = false)
    private String kind;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "visitor_id")
    private String visitorId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Offering() {}

    public Offering(String id, String space, String kind, String authorName, String visitorId, Instant createdAt) {
        this.id = id;
        this.space = space;
        this.kind = kind;
        this.authorName = authorName;
        this.visitorId = visitorId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getKind() { return kind; }
    public String getAuthorName() { return authorName; }
    public Instant getCreatedAt() { return createdAt; }
}
