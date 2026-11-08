package com.memorial.server.live;

import com.memorial.server.common.Ids;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** 연결(세션)마다 지금 어느 공간에 어떤 모습으로 있는지 메모리에 둔다. 서버를 다시 띄우면 비고, 화면이 다시 보낸다. */
@Component
public class PresenceRegistry {

    public record Peer(String peer, Map<String, Object> state) {}

    private record Entry(String peer, String space, Map<String, Object> state) {}

    /** update 결과: 새 상태와, 다른 공간에서 옮겨 왔다면 그 공간. */
    public record Moved(Peer peer, String fromSpace, boolean joined) {}

    private final Map<String, Entry> bySession = new ConcurrentHashMap<>();

    public Moved update(String sessionId, String space, Map<String, Object> state) {
        Entry old = bySession.get(sessionId);
        String peer = old != null ? old.peer() : Ids.random(10);
        bySession.put(sessionId, new Entry(peer, space, state));
        String from = old != null && !old.space().equals(space) ? old.space() : null;
        return new Moved(new Peer(peer, state), from, old == null || from != null);
    }

    /** 나간 사람의 peer와 공간. 없었으면 null. */
    public String[] leave(String sessionId) {
        Entry e = bySession.remove(sessionId);
        return e == null ? null : new String[] {e.peer(), e.space()};
    }

    public List<Peer> snapshot(String space) {
        List<Peer> out = new ArrayList<>();
        for (Entry e : bySession.values()) if (e.space().equals(space)) out.add(new Peer(e.peer(), e.state()));
        return out;
    }

    public Map<String, Integer> counts() {
        Map<String, Integer> out = new TreeMap<>();
        for (Entry e : bySession.values()) out.merge(e.space(), 1, Integer::sum);
        return out;
    }
}
