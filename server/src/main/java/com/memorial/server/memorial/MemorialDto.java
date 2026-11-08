package com.memorial.server.memorial;

import java.time.Instant;

public record MemorialDto(String id, String name, String years, String intro, boolean hasPhoto, String createdBy, Instant createdAt) {

    static MemorialDto of(Memorial m) {
        return new MemorialDto(m.getId(), m.getName(), m.getYears(), m.getIntro(), m.getPhotoType() != null, m.getCreatedBy(), m.getCreatedAt());
    }
}
