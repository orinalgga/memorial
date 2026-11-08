package com.memorial.server.common;

import java.util.Base64;
import java.util.Set;

/** 화면이 보내는 data:image/...;base64,... 사진을 바이트로 바꾼다. */
public final class Images {

    private static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private Images() {}

    public record Decoded(String contentType, byte[] bytes) {}

    public static Decoded fromDataUrl(String dataUrl, int maxBytes) {
        if (dataUrl == null || !dataUrl.startsWith("data:")) throw ApiException.badRequest("사진 형식을 읽을 수 없어요.");
        int comma = dataUrl.indexOf(',');
        int semi = dataUrl.indexOf(';');
        if (comma < 0 || semi < 0 || semi > comma || !dataUrl.substring(semi, comma).equals(";base64")) {
            throw ApiException.badRequest("사진 형식을 읽을 수 없어요.");
        }
        String type = dataUrl.substring(5, semi);
        if (!TYPES.contains(type)) throw ApiException.badRequest("JPG, PNG, WEBP 사진만 올릴 수 있어요.");
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(dataUrl.substring(comma + 1));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("사진 형식을 읽을 수 없어요.");
        }
        if (bytes.length == 0) throw ApiException.badRequest("사진이 비어 있어요.");
        if (bytes.length > maxBytes) throw ApiException.badRequest("사진이 너무 커요. 더 작은 사진으로 올려 주세요.");
        return new Decoded(type, bytes);
    }
}
