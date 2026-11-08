package com.memorial.server.avatar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** 방문자가 고른 얼굴 사진. 다른 사람 화면에서 그 사람 캐릭터의 얼굴로 쓴다. */
@Entity
@Table(name = "avatar_faces")
public class AvatarFace {

    @Id
    @Column(name = "visitor_id")
    private String visitorId;

    @Column(nullable = false)
    private byte[] image;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AvatarFace() {}

    public AvatarFace(String visitorId, byte[] image, String contentType, Instant updatedAt) {
        this.visitorId = visitorId;
        this.image = image;
        this.contentType = contentType;
        this.updatedAt = updatedAt;
    }

    public byte[] getImage() { return image; }
    public String getContentType() { return contentType; }
}
