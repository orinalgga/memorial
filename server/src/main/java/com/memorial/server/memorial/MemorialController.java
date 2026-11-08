package com.memorial.server.memorial;

import java.time.Duration;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/memorials")
public class MemorialController {

    private final MemorialService service;

    public MemorialController(MemorialService service) {
        this.service = service;
    }

    @GetMapping
    public List<MemorialDto> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemorialService.Created create(@RequestBody MemorialRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public MemorialDto update(@PathVariable String id, @RequestHeader(value = "X-Edit-Token", required = false) String token,
            @RequestBody MemorialRequest req) {
        return service.update(id, token, req);
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable String id) {
        Memorial m = service.get(id);
        if (m.getPhoto() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(m.getPhotoType()))
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(m.getPhoto());
    }
}
