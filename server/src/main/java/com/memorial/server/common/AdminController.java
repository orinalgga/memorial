package com.memorial.server.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** 화면이 관리자 모드를 켜기 전에 토큰이 맞는지 확인한다. POST라서 글쓰기 횟수 제한을 함께 받는다(무차별 대입 방지). */
@RestController
public class AdminController {

    private final Access access;

    public AdminController(Access access) {
        this.access = access;
    }

    @PostMapping("/api/admin/check")
    public ResponseEntity<Void> check(@RequestHeader(value = Access.ADMIN_HEADER, required = false) String token) {
        if (!access.adminEnabled()) throw ApiException.forbidden("관리자 기능이 꺼져 있어요. 서버에 MEMORIAL_ADMIN_TOKEN을 넣어 주세요.");
        if (!access.isAdmin(token)) throw ApiException.forbidden("관리자 토큰이 맞지 않아요.");
        return ResponseEntity.noContent().build();
    }
}
