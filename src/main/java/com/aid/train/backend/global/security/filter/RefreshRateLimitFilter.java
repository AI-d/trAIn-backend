package com.aid.train.backend.global.security.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Refresh Token 재발급 엔드포인트에 대한 Rate Limiting 필터.
 *
 * <p>보안 강화 포인트:
 * <ul>
 *   <li>정확 경로/메서드 매칭 (POST /api/v1/users/refresh)</li>
 *   <li>CORS 프리플라이트(OPTIONS) 즉시 패스</li>
 *   <li>X-Forwarded-For 기반 실제 클라이언트 IP 식별</li>
 *   <li>Caffeine 캐시로 버킷을 접근기준 만료(expireAfterAccess) + 최대 엔트리 제한</li>
 *   <li>X-RateLimit-Remaining, Retry-After 헤더 제공</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Component
public class RefreshRateLimitFilter extends OncePerRequestFilter {

    // --- 정책 상수(필요시 yml로 뺄 수 있음) ---
    private static final String TARGET_PATH = "/api/v1/users/refresh";
    private static final int WINDOW_SECONDS = 10;   // 윈도우: 10초 (더 짧게)
    private static final int CAPACITY = 2;          // 버킷 최대 토큰 (더 적게)
    private static final int REFILL_TOKENS = 2;     // 윈도우당 보충 토큰 수

    // 캐시: 접근기준 10분 동안 사용 없으면 제거, 최대 100k 엔트리
    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // 1) 프리플라이트는 통과
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        // 2) 정확한 엔드포인트/메서드만 제한
        if (!isRefreshEndpoint(req)) {
            chain.doFilter(req, res);
            return;
        }

        // 3) 클라이언트 키 산출 (프록시 고려: X-Forwarded-For)
        String clientIp = resolveClientIp(req);
        String userAgent = Optional.ofNullable(req.getHeader("User-Agent")).orElse("unknown");
        String key = clientIp + ":" + userAgent;

        // 4) 버킷 획득
        Bucket bucket = bucketCache.get(key, k -> newBucket());

        // 5) 소비 + 남은 토큰/대기 시간 확인
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // 관측/디버깅용 헤더
            res.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(req, res);
            return;
        }

        // 6) 초과 시 429 + Retry-After
        long nanosToWait = probe.getNanosToWaitForRefill();
        long secondsToWait = Math.max(1, Duration.ofNanos(nanosToWait).toSeconds());

        res.setStatus(429);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Retry-After", String.valueOf(secondsToWait));
        res.getWriter().write("{\"code\":\"RATE_LIMIT\",\"message\":\"Too many refresh requests\"}");
        res.getWriter().flush();
    }

    // --- Helpers ---

    private boolean isRefreshEndpoint(HttpServletRequest req) {
        return "POST".equalsIgnoreCase(req.getMethod())
                && (TARGET_PATH.equals(req.getRequestURI()) || req.getRequestURI().startsWith(TARGET_PATH + "/"));
    }

    private String resolveClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // 첫 번째 항목이 클라이언트 IP
            return xff.split(",")[0].trim();
        }
        String realIp = req.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return req.getRemoteAddr();
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(CAPACITY)
                .refillGreedy(REFILL_TOKENS, Duration.ofSeconds(WINDOW_SECONDS))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
