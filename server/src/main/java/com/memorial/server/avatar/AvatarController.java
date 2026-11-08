package com.memorial.server.avatar;

import com.memorial.server.common.ApiException;
import com.memorial.server.common.Ids;
import com.memorial.server.common.Images;
import com.memorial.server.common.Limits;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AvatarController {

    public record FaceRequest(String image) {}

    private final AvatarFaceRepository repo;
    private final Limits limits;

    public AvatarController(AvatarFaceRepository repo, Limits limits) {
        this.repo = repo;
        this.limits = limits;
    }

    @PutMapping("/api/avatars/{visitorId}/face")
    @Transactional
    public ResponseEntity<Void> put(@PathVariable String visitorId, @RequestBody FaceRequest req) {
        check(visitorId);
        Images.Decoded img = Images.fromDataUrl(req.image(), limits.faceBytes());
        repo.save(new AvatarFace(visitorId, img.bytes(), img.contentType(), Instant.now()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/avatars/{visitorId}/face")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable String visitorId) {
        check(visitorId);
        repo.deleteById(visitorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/avatars/{visitorId}/face")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> get(@PathVariable String visitorId) {
        check(visitorId);
        return repo.findById(visitorId)
                .map(f -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(f.getContentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                        .body(f.getImage()))
                .orElse(ResponseEntity.notFound().build());
    }

    private static void check(String visitorId) {
        if (!Ids.isVisitorId(visitorId)) throw ApiException.badRequest("방문자 정보가 올바르지 않아요.");
    }
}
