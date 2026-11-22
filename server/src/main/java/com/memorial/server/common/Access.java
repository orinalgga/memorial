package com.memorial.server.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 누가 글을 지울 수 있는지 정한다.
 * - 글쓴이: 브라우저가 한 번 만들어 둔 비밀 키(ownerKey)를 글을 쓸 때 함께 보낸다. DB에는 그 해시만 두고,
 *   화면에는 해시의 앞 16자(owner)만 보여 줘서 "내 글"을 알아보게 한다. 방문자 id는 남에게도 보이므로 쓰지 않는다.
 * - 관리자: 서버 환경 변수 MEMORIAL_ADMIN_TOKEN과 같은 값을 X-Admin-Token 헤더로 보낸다. 비어 있으면 관리자 기능은 꺼진다.
 */
@Component
public class Access {

    public static final String OWNER_HEADER = "X-Owner-Key";
    public static final String ADMIN_HEADER = "X-Admin-Token";

    private final byte[] adminToken;

    public Access(@Value("${memorial.admin-token:}") String adminToken) {
        String t = adminToken == null ? "" : adminToken.trim();
        this.adminToken = t.length() >= 16 ? t.getBytes(StandardCharsets.UTF_8) : null;
    }

    public boolean adminEnabled() {
        return adminToken != null;
    }

    public boolean isAdmin(String token) {
        return adminToken != null && token != null && MessageDigest.isEqual(adminToken, token.trim().getBytes(StandardCharsets.UTF_8));
    }

    /** 키가 올바른 모양이면 해시를, 아니면 null을 돌려준다. */
    public static String ownerHash(String ownerKey) {
        if (ownerKey == null || !ownerKey.matches("[a-z0-9]{20,64}")) return null;
        return sha256(ownerKey);
    }

    /** 화면에 보여 주는 글쓴이 표시. 화면은 자기 키로 같은 값을 만들어 비교한다. */
    public static String tag(String ownerHash) {
        return ownerHash == null ? null : ownerHash.substring(0, 16);
    }

    /** 글쓴이 본인이거나 관리자일 때만 통과한다. */
    public void requireOwnerOrAdmin(String storedHash, String ownerKey, String adminToken, String what) {
        if (isAdmin(adminToken)) return;
        String h = ownerHash(ownerKey);
        if (storedHash != null && h != null
                && MessageDigest.isEqual(storedHash.getBytes(StandardCharsets.UTF_8), h.getBytes(StandardCharsets.UTF_8))) return;
        throw ApiException.forbidden(what + "을(를) 쓴 분만 지울 수 있어요.");
    }

    public static String sha256(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
