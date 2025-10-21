package com.aid.train.backend.global.security.jwt;

import com.aid.train.backend.global.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 토큰을 검증하고 인증 정보를 설정하는 필터입니다.
 * 요청마다 한 번씩 실행됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    /**
     * JWT 토큰을 검증하고 인증 정보를 SecurityContext에 설정합니다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. 이미 인증된 컨텍스트면 재설정 금지 (중복 필터 진입 방지, 성능 향상)
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Request에서 JWT 토큰 추출 (Cookie 우선, 없으면 Header)
            String jwt = getJwtFromRequest(request);

            // 3. 토큰 검증 및 사용자 ID 추출
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                
                // 4. ACCESS 토큰인지 확인 (다른 타입 토큰은 인증 불가)
                if (!jwtTokenProvider.isAccessToken(jwt)) {
                    log.warn("인증에 사용할 수 없는 토큰 타입 - URI: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

                // 5. Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("사용자 인증 완료 - User ID: {}, URI: {}", userId, request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("SecurityContext에 인증 정보를 설정할 수 없습니다.", ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP Request에서 JWT 토큰을 추출합니다.
     * 우선순위: Cookie > Authorization Header
     *
     * @param request HTTP 요청
     * @return JWT 토큰
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Cookie에서 토큰 추출 (우선)
        String tokenFromCookie = cookieUtil.getAccessToken(request).orElse(null);
        if (StringUtils.hasText(tokenFromCookie)) {
            log.debug("Cookie에서 Access Token 추출 - URI: {}", request.getRequestURI());
            return tokenFromCookie;
        }

        // 2. Authorization Header에서 토큰 추출 (백업)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.debug("Authorization Header에서 Access Token 추출 - URI: {}", request.getRequestURI());
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * 필터 적용 여부 결정
     * Public 엔드포인트들은 JWT 필터를 거치지 않도록 설정
     *
     * @param request HTTP 요청 객체
     * @return 필터를 적용하지 않으려면 true, 적용하려면 false
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // JWT 필터를 적용하지 않을 경로들 (Public 엔드포인트)
        return path.equals("/") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/error") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") ||
                // 인증이 필요없는 Auth 엔드포인트들
                path.equals("/api/v1/auth/signup") ||
                path.equals("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/email/") ||
                path.startsWith("/api/v1/auth/social/") ||
                path.startsWith("/api/v1/auth/refresh") ||
                path.startsWith("/api/v1/terms");
    }

    /**
     * 현재 인증된 사용자 ID 추출 유틸리티 메서드
     *
     * @return 현재 인증된 사용자 ID, 인증되지 않은 경우 null
     */
    public static Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            }
        } catch (Exception e) {
            log.debug("현재 사용자 ID 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 현재 인증된 사용자의 인증 여부 확인
     *
     * @return 인증된 사용자가 있으면 true, 없으면 false
     */
    public static boolean isAuthenticated() {
        try {
            return SecurityContextHolder.getContext().getAuthentication() != null &&
                    SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                    !SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * SecurityContext 클리어 유틸리티 메서드
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        log.debug("Security Context 클리어 완료");
    }
}