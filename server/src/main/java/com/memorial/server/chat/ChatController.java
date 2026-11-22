package com.memorial.server.chat;

import com.memorial.server.common.Access;
import com.memorial.server.common.ApiException;
import com.memorial.server.common.Ids;
import com.memorial.server.common.Spaces;
import com.memorial.server.common.Text;
import com.memorial.server.live.Events;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 공간마다 따로 있는 대화. 들어온 사람은 최근 40개를 받는다. */
@RestController
public class ChatController {

    public record ChatDto(String id, String name, String visitorId, String text, Instant createdAt, String owner) {
        static ChatDto of(ChatMessage m) {
            return new ChatDto(m.getId(), m.getAuthorName(), m.getVisitorId(), m.getText(), m.getCreatedAt(), Access.tag(m.getOwnerHash()));
        }
    }

    public record NewChat(String text, String name, String visitorId, String ownerKey) {}

    private final ChatRepository repo;
    private final Spaces spaces;
    private final Events events;
    private final Access access;

    public ChatController(ChatRepository repo, Spaces spaces, Events events, Access access) {
        this.access = access;
        this.repo = repo;
        this.spaces = spaces;
        this.events = events;
    }

    @GetMapping("/api/spaces/{space}/chat")
    @Transactional(readOnly = true)
    public List<ChatDto> recent(@PathVariable String space) {
        spaces.require(space);
        List<ChatDto> out = new ArrayList<>(repo.findBySpaceOrderByCreatedAtDesc(space, PageRequest.of(0, 40)).stream().map(ChatDto::of).toList());
        Collections.reverse(out);
        return out;
    }

    @PostMapping("/api/spaces/{space}/chat")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ChatDto send(@PathVariable String space, @RequestBody NewChat req) {
        spaces.require(space);
        ChatMessage m = new ChatMessage(Ids.newId(), space, Text.name(req.name()), Ids.isVisitorId(req.visitorId()) ? req.visitorId() : null,
                Text.required(req.text() == null ? null : req.text().replace("\n", " "), 120, "메시지"), Instant.now());
        m.setOwnerHash(Access.ownerHash(req.ownerKey()));
        repo.save(m);
        ChatDto dto = ChatDto.of(m);
        events.space(space, "chat", dto);
        return dto;
    }

    @DeleteMapping("/api/chat/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable String id, @RequestHeader(value = Access.OWNER_HEADER, required = false) String ownerKey,
            @RequestHeader(value = Access.ADMIN_HEADER, required = false) String adminToken) {
        ChatMessage m = repo.findById(id).orElseThrow(() -> ApiException.notFound("찾을 수 없는 메시지예요."));
        access.requireOwnerOrAdmin(m.getOwnerHash(), ownerKey, adminToken, "메시지");
        repo.delete(m);
        events.space(m.getSpace(), "chat-deleted", Map.of("id", id));
    }
}
