package com.memorial.server.letter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "letter_hearts")
@IdClass(LetterHeart.Key.class)
public class LetterHeart {

    public record Key(String letterId, String visitorId) implements Serializable {
        public Key() {
            this(null, null);
        }
    }

    @Id
    @Column(name = "letter_id")
    private String letterId;

    @Id
    @Column(name = "visitor_id")
    private String visitorId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LetterHeart() {}

    public LetterHeart(String letterId, String visitorId, Instant createdAt) {
        this.letterId = letterId;
        this.visitorId = visitorId;
        this.createdAt = createdAt;
    }
}
