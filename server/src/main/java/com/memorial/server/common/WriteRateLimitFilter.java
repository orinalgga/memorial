package com.memorial.server.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 로그인이 없어서 글쓰기 요청을 IP마다 1분에 정해 둔 횟수로 막는다. 서버 한 대 기준의 간단한 고정 창 방식이다.
 */
@Component
public class WriteRateLimitFilter extends OncePerRequestFilter {

    private final Limits limits;
    private final Map<String, long[]> windows = new ConcurrentHashMap<>();

    public WriteRateLimitFilter(Limits limits) {
        this.limits = limits;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String m = request.getMethod();
        return !request.getRequestURI().startsWith("/api/") || "GET".equals(m) || "OPTIONS".equals(m) || "HEAD".equals(m);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long minute = System.currentTimeMillis() / 60_000;
        long[] w = windows.compute(request.getRemoteAddr(), (ip, old) -> {
            if (old == null || old[0] != minute) return new long[] {minute, 1};
            old[1]++;
            return old;
        });
        if (windows.size() > 50_000) windows.entrySet().removeIf(e -> e.getValue()[0] != minute);
        if (w[1] > limits.writesPerMinute()) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getOutputStream().write("{\"message\":\"잠시 뒤에 다시 시도해 주세요.\"}".getBytes(StandardCharsets.UTF_8));
            return;
        }
        chain.doFilter(request, response);
    }
}
