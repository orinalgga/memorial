package com.memorial.server.memorial;

/** photo는 data:image/jpeg;base64,... 형식. 수정할 때 빈 문자열이면 사진을 지우고, null이면 그대로 둔다. */
public record MemorialRequest(String name, String years, String intro, String photo, String createdBy) {}
