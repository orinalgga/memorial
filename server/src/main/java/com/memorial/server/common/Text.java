package com.memorial.server.common;

/** 사람이 쓴 글을 저장하기 전에 다듬는다. 화면은 항상 textContent로 넣으므로 HTML 이스케이프는 하지 않는다. */
public final class Text {

    private Text() {}

    /** 앞뒤 공백과 보이지 않는 제어 문자를 지우고, 비었으면 null. */
    public static String clean(String s, int max) {
        if (s == null) return null;
        String t = s.replaceAll("[\\p{Cc}&&[^\\n]]", "").strip();
        if (t.isEmpty()) return null;
        return t.length() > max ? t.substring(0, max) : t;
    }

    public static String required(String s, int max, String what) {
        String t = clean(s, max);
        if (t == null) throw ApiException.badRequest(what + "을(를) 입력해 주세요.");
        return t;
    }

    public static String name(String s) {
        String t = clean(s == null ? null : s.replace("\n", " "), 16);
        return t == null ? "이름 없는 이" : t;
    }
}
