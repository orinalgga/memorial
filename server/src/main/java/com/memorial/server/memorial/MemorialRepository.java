package com.memorial.server.memorial;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemorialRepository extends JpaRepository<Memorial, String> {

    /** 목록에는 사진 바이트를 싣지 않는다. */
    @Query("select new com.memorial.server.memorial.MemorialDto(m.id, m.name, m.years, m.intro, "
            + "case when m.photoType is null then false else true end, m.createdBy, m.createdAt) "
            + "from Memorial m order by m.createdAt desc")
    List<MemorialDto> listAll();
}
