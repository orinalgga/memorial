package com.memorial.server.memorial;

import com.memorial.server.common.ApiException;
import com.memorial.server.common.Ids;
import com.memorial.server.common.Images;
import com.memorial.server.common.Limits;
import com.memorial.server.common.Text;
import com.memorial.server.live.Events;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개인 추모 공간. 로그인이 없으므로 만든 사람에게 수정용 토큰을 한 번만 돌려주고, DB에는 그 해시만 둔다.
 */
@Service
public class MemorialService {

    private final MemorialRepository repo;
    private final Limits limits;
    private final Events events;

    public MemorialService(MemorialRepository repo, Limits limits, Events events) {
        this.repo = repo;
        this.limits = limits;
        this.events = events;
    }

    public record Created(MemorialDto memorial, String editToken) {}

    public List<MemorialDto> list() {
        return repo.listAll();
    }

    @Transactional
    public Created create(MemorialRequest req) {
        String token = Ids.random(32);
        Memorial m = new Memorial(Ids.newId(), Text.required(req.name(), 20, "기억할 분의 이름"),
                Text.clean(req.years(), 30), Text.clean(req.intro(), 60), Text.name(req.createdBy()), hash(token), Instant.now());
        if (req.photo() != null && !req.photo().isBlank()) {
            Images.Decoded img = Images.fromDataUrl(req.photo(), limits.photoBytes());
            m.setPhoto(img.bytes(), img.contentType());
        }
        repo.save(m);
        MemorialDto dto = MemorialDto.of(m);
        events.memorial(dto);
        return new Created(dto, token);
    }

    @Transactional
    public MemorialDto update(String id, String token, MemorialRequest req) {
        Memorial m = owned(id, token);
        m.update(Text.required(req.name(), 20, "기억할 분의 이름"), Text.clean(req.years(), 30), Text.clean(req.intro(), 60));
        if (req.photo() != null) {
            if (req.photo().isBlank()) m.setPhoto(null, null);
            else {
                Images.Decoded img = Images.fromDataUrl(req.photo(), limits.photoBytes());
                m.setPhoto(img.bytes(), img.contentType());
            }
        }
        MemorialDto dto = MemorialDto.of(m);
        events.memorial(dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public Memorial get(String id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("찾을 수 없는 추모 공간이에요."));
    }

    private Memorial owned(String id, String token) {
        Memorial m = get(id);
        if (token == null || !MessageDigest.isEqual(hash(token).getBytes(StandardCharsets.UTF_8),
                m.getEditTokenHash().getBytes(StandardCharsets.UTF_8))) {
            throw ApiException.forbidden("이 추모 공간을 만든 분만 고칠 수 있어요.");
        }
        return m;
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
