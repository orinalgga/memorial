package com.memorial.server.common;

import com.memorial.server.memorial.MemorialRepository;
import org.springframework.stereotype.Component;

/** 공간 이름: 'garden'(공용 추모 공간) 또는 개인 추모 공간 id. */
@Component
public class Spaces {

    public static final String GARDEN = "garden";

    private final MemorialRepository memorials;

    public Spaces(MemorialRepository memorials) {
        this.memorials = memorials;
    }

    public String require(String space) {
        if (GARDEN.equals(space)) return space;
        if (Ids.isEntityId(space) && memorials.existsById(space)) return space;
        throw ApiException.notFound("찾을 수 없는 추모 공간이에요.");
    }

    public static boolean looksValid(String space) {
        return GARDEN.equals(space) || Ids.isEntityId(space);
    }
}
