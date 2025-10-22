package com.aid.train.backend.global.util;

import com.aid.train.backend.global.properties.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * HTTP 쿠키 관련 작업을 처리하는 유틸리티 클래스입니다.
 * <p>
 * Refresh Token을 안전한 HttpOnly 쿠키로 생성하고,
 * 요청 헤더에서 쿠키를 읽어오는 기능을 제공합니다.
 * </p>
 *
 * @author 왕택준
 * @see CookieProperties
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final CookieProperties cookieProperties;

    /**
     * Refresh Token을 담은 HttpOnly 쿠키를 생성하여 응답에 추가합니다.
     *
     * @param response     HttpServletResponse 객체
     * @param refreshToken 저장할 Refresh Token
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.getRefreshTokenName(), refreshToken)
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(cookieProperties.getRefreshTokenMaxAge())
                .sameSite(cookieProperties.getSameSite())
                .domain(cookieProperties.getDomain())
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 요청에서 Refresh Token 쿠키를 찾아 반환합니다.
     *
     * @param request HttpServletRequest 객체
     * @return Refresh Token 쿠키의 값 (Optional)
     */
    public Optional<String> getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieProperties.getRefreshTokenName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Refresh Token 쿠키를 삭제합니다.
     * (maxAge를 0으로 설정하여 즉시 만료시킴)
     *
     * @param response HttpServletResponse 객체
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.getRefreshTokenName(), "")
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(0) // 즉시 만료
                .sameSite(cookieProperties.getSameSite())
                .domain(cookieProperties.getDomain())
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}