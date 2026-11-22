package com.memorial.server.offering;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OfferingRepository extends JpaRepository<Offering, String> {

    List<Offering> findBySpaceOrderByCreatedAtAsc(String space, Pageable page);

    long countBySpaceAndKind(String space, String kind);

    @Modifying
    @Query("delete from Offering o where o.space = :space")
    int deleteBySpace(String space);
}
