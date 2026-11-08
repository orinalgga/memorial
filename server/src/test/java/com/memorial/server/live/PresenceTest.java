package com.memorial.server.live;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PresenceTest {

    @Test
    void sanitizeKeepsOnlyKnownFieldsInRange() {
        Map<String, Object> in = new HashMap<>();
        in.put("x", 999);
        in.put("z", -3.14159);
        in.put("n", "  봄비\u0007아주아주아주아주긴이름입니다 ");
        in.put("o", "red");
        in.put("k", "visitor001");
        in.put("s", "x");
        in.put("a", "senior");
        in.put("h", 1);
        in.put("evil", "<script>");
        Map<String, Object> out = PresenceState.sanitize(in);
        assertThat(out).doesNotContainKeys("evil", "o");
        assertThat(out.get("x")).isEqualTo(60.0);
        assertThat(out.get("z")).isEqualTo(-3.14);
        assertThat((String) out.get("n")).hasSize(16).doesNotContain("\u0007");
        assertThat(out.get("s")).isEqualTo("n");
        assertThat(out.get("a")).isEqualTo("senior");
        assertThat(out.get("h")).isEqualTo(1);
    }

    @Test
    void movingBetweenSpacesAndLeavingUpdatesCounts() {
        PresenceRegistry r = new PresenceRegistry();
        PresenceRegistry.Moved first = r.update("s1", "garden", Map.of());
        assertThat(first.joined()).isTrue();
        assertThat(r.update("s1", "garden", Map.of()).joined()).isFalse();
        r.update("s2", "garden", Map.of());
        assertThat(r.counts()).containsEntry("garden", 2);

        PresenceRegistry.Moved moved = r.update("s1", "abc123def", Map.of());
        assertThat(moved.fromSpace()).isEqualTo("garden");
        assertThat(moved.peer().peer()).isEqualTo(first.peer().peer());
        assertThat(r.counts()).containsEntry("garden", 1).containsEntry("abc123def", 1);
        assertThat(r.snapshot("abc123def")).hasSize(1);

        String[] gone = r.leave("s2");
        assertThat(gone).hasSize(2);
        assertThat(gone[1]).isEqualTo("garden");
        assertThat(r.counts()).doesNotContainKey("garden").containsEntry("abc123def", 1);
        assertThat(r.leave("s2")).isNull();
    }
}
