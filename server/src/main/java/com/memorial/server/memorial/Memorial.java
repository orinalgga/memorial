package com.memorial.server.memorial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "memorials")
public class Memorial {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String years;
    private String intro;

    private byte[] photo;

    @Column(name = "photo_type")
    private String photoType;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "edit_token_hash", nullable = false)
    private String editTokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Memorial() {}

    public Memorial(String id, String name, String years, String intro, String createdBy, String editTokenHash, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.years = years;
        this.intro = intro;
        this.createdBy = createdBy;
        this.editTokenHash = editTokenHash;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getYears() { return years; }
    public String getIntro() { return intro; }
    public byte[] getPhoto() { return photo; }
    public String getPhotoType() { return photoType; }
    public String getCreatedBy() { return createdBy; }
    public String getEditTokenHash() { return editTokenHash; }
    public Instant getCreatedAt() { return createdAt; }

    public void update(String name, String years, String intro) {
        this.name = name;
        this.years = years;
        this.intro = intro;
    }

    public void setPhoto(byte[] photo, String type) {
        this.photo = photo;
        this.photoType = type;
    }
}
