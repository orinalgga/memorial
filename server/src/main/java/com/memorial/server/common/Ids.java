package com.memorial.server.common;

import java.security.SecureRandom;

/** 주소(#m-<id>)와 실시간 방 이름에 그대로 쓸 수 있게 소문자와 숫자만 쓴다. */
public final class Ids {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private Ids() {}

    public static String newId() {
        return random(14);
    }

    public static String random(int length) {
        char[] out = new char[length];
        for (int i = 0; i < length; i++) out[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        return new String(out);
    }

    /** 방문자 id는 화면이 브라우저마다 한 번 만들어 둔다. */
    public static boolean isVisitorId(String s) {
        return s != null && s.matches("[a-z0-9]{6,40}");
    }

    public static boolean isEntityId(String s) {
        return s != null && s.matches("[a-z0-9]{6,32}");
    }
}
