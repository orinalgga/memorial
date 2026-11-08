package com.memorial.server.offering;

import com.memorial.server.common.ApiException;
import com.memorial.server.common.Ids;
import com.memorial.server.common.Spaces;
import com.memorial.server.common.Text;
import com.memorial.server.live.Events;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OfferingController {

    private static final Set<String> KINDS = Set.of("candle", "flower");

    public record OfferingDto(String id, String kind, String name, Instant createdAt) {
        static OfferingDto of(Offering o) {
            return new OfferingDto(o.getId(), o.getKind(), o.getAuthorName(), o.getCreatedAt());
        }
    }

    /** 화면은 제단에 올려 둘 만큼(최근 500개)만 받고, 전체 수는 따로 받는다. */
    public record OfferingList(long candles, long flowers, List<OfferingDto> items) {}

    public record NewOffering(String kind, String name, String visitorId) {}

    private final OfferingRepository repo;
    private final Spaces spaces;
    private final Events events;

    public OfferingController(OfferingRepository repo, Spaces spaces, Events events) {
        this.repo = repo;
        this.spaces = spaces;
        this.events = events;
    }

    @GetMapping("/api/spaces/{space}/offerings")
    @Transactional(readOnly = true)
    public OfferingList list(@PathVariable String space) {
        spaces.require(space);
        List<OfferingDto> items = repo.findBySpaceOrderByCreatedAtAsc(space, PageRequest.of(0, 500)).stream().map(OfferingDto::of).toList();
        return new OfferingList(repo.countBySpaceAndKind(space, "candle"), repo.countBySpaceAndKind(space, "flower"), items);
    }

    @PostMapping("/api/spaces/{space}/offerings")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public OfferingDto offer(@PathVariable String space, @RequestBody NewOffering req) {
        spaces.require(space);
        if (!KINDS.contains(req.kind())) throw ApiException.badRequest("촛불이나 국화만 올릴 수 있어요.");
        Offering o = new Offering(Ids.newId(), space, req.kind(), Text.name(req.name()),
                Ids.isVisitorId(req.visitorId()) ? req.visitorId() : null, Instant.now());
        repo.save(o);
        OfferingDto dto = OfferingDto.of(o);
        events.space(space, "offering", dto);
        return dto;
    }
}
