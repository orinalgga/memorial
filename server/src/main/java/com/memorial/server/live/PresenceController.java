package com.memorial.server.live;

import com.memorial.server.common.Spaces;
import java.util.List;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * 함께 있는 사람들. 화면은 움직일 때 /app/spaces/{space}/presence로 자기 모습을 보내고,
 * 들어갈 때 /app/spaces/{space}/peers를 한 번 구독해 이미 있던 사람들을 받는다.
 */
@Controller
public class PresenceController {

    private final PresenceRegistry registry;
    private final SimpMessagingTemplate broker;

    public PresenceController(PresenceRegistry registry, SimpMessagingTemplate broker) {
        this.registry = registry;
        this.broker = broker;
    }

    @MessageMapping("/spaces/{space}/presence")
    public void presence(@DestinationVariable String space, @Payload Map<String, Object> body, SimpMessageHeaderAccessor headers) {
        if (!Spaces.looksValid(space) || headers.getSessionId() == null) return;
        PresenceRegistry.Moved moved = registry.update(headers.getSessionId(), space, PresenceState.sanitize(body));
        if (moved.fromSpace() != null) left(moved.fromSpace(), moved.peer().peer());
        broker.convertAndSend(topic(space), Map.of("peer", moved.peer().peer(), "state", moved.peer().state()));
        if (moved.joined()) broker.convertAndSend("/topic/visitors", registry.counts());
    }

    @MessageMapping("/presence/leave")
    public void leave(SimpMessageHeaderAccessor headers) {
        leaveSession(headers.getSessionId());
    }

    @SubscribeMapping("/spaces/{space}/peers")
    public List<PresenceRegistry.Peer> peers(@DestinationVariable String space) {
        return Spaces.looksValid(space) ? registry.snapshot(space) : List.of();
    }

    @SubscribeMapping("/visitors")
    public Map<String, Integer> visitors() {
        return registry.counts();
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        leaveSession(event.getSessionId());
    }

    private void leaveSession(String sessionId) {
        if (sessionId == null) return;
        String[] gone = registry.leave(sessionId);
        if (gone == null) return;
        left(gone[1], gone[0]);
        broker.convertAndSend("/topic/visitors", registry.counts());
    }

    private void left(String space, String peer) {
        broker.convertAndSend(topic(space), Map.of("peer", peer, "left", true));
    }

    private static String topic(String space) {
        return "/topic/spaces/" + space + "/presence";
    }
}
