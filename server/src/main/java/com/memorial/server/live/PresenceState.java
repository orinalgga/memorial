package com.memorial.server.live;

import com.memorial.server.common.Ids;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** 화면이 보낸 위치·모습에서 아는 값만 골라 범위를 맞춘다. 다른 사람에게 그대로 전달되므로 꼭 거른다. */
final class PresenceState {

    private static final Set<String> SEXES = Set.of("f", "m", "n");
    private static final Set<String> AGES = Set.of("child", "young", "middle", "senior");

    private PresenceState() {}

    static Map<String, Object> sanitize(Map<String, Object> in) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("x", clamp(num(in.get("x")), 60));
        out.put("z", clamp(num(in.get("z")), 60));
        out.put("r", clamp(num(in.get("r")), 10));
        Object n = in.get("n");
        String name = n instanceof String s ? s.replaceAll("\\p{Cc}", "").strip() : "";
        out.put("n", name.isEmpty() ? "방문자" : name.substring(0, Math.min(16, name.length())));
        Object o = in.get("o");
        if (o instanceof String s && s.matches("#[0-9a-fA-F]{6}")) out.put("o", s);
        Object k = in.get("k");
        if (k instanceof String s && Ids.isVisitorId(s)) out.put("k", s);
        Object sx = in.get("s");
        out.put("s", sx instanceof String s && SEXES.contains(s) ? s : "n");
        Object a = in.get("a");
        out.put("a", a instanceof String s && AGES.contains(s) ? s : "young");
        out.put("h", Integer.valueOf(1).equals(in.get("h")) || Boolean.TRUE.equals(in.get("h")) ? 1 : 0);
        return out;
    }

    private static double num(Object v) {
        return v instanceof Number n && Double.isFinite(n.doubleValue()) ? n.doubleValue() : 0;
    }

    private static double clamp(double v, double lim) {
        return Math.round(Math.max(-lim, Math.min(lim, v)) * 100) / 100.0;
    }
}
