package com.aid.train.backend.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * HTTP 쿠키 생성 및 관리를 담당하는 유틸리티 클래스입니다.
 * JWT 토큰을 안전하게 쿠키에 저장하고 관리합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Component
public class CookieUtil {

    /**
     * Access Token 쿠키 이름
     */
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    /**
     * Refresh Token 쿠키 이름
     */
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    /**
     * HttpOnly, Secure 쿠키를 생성합니다.
     *
     * @param name     쿠키 이름
     * @param value    쿠키 값
     * @param maxAge   쿠키 유효 시간 (초)
     * @param response HTTP 응답 객체
     */
    public void addCookie(String name, String value, long maxAge, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")                           // 모든 경로에서 사용
                .httpOnly(true)                      // JavaScript 접근 불가 (XSS 방어)
                .secure(true)                        // HTTPS에서만 전송 (프로덕션)
                .sameSite("Strict")                  // CSRF 방어
                .maxAge(Duration.ofSeconds(maxAge))  // 유효 시간
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Access Token 쿠키를 생성합니다.
     *
     * @param accessToken Access Token 값
     * @param maxAge      유효 시간 (초)
     * @param response    HTTP 응답 객체
     */
    public void addAccessTokenCookie(String accessToken, long maxAge, HttpServletResponse response) {
        addCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken, maxAge, response);
    }

    /**
     * Refresh Token 쿠키를 생성합니다.
     *
     * @param refreshToken Refresh Token 값
     * @param maxAge       유효 시간 (초)
     * @param response     HTTP 응답 객체
     */
    public void addRefreshTokenCookie(String refreshToken, long maxAge, HttpServletResponse response) {
        addCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, maxAge, response);
    }

    /**
     * 쿠키를 조회합니다.
     *
     * @param request HTTP 요청 객체
     * @param name    쿠키 이름
     * @return 쿠키 값 (Optional)
     */
    public Optional<String> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(name))
                    .map(Cookie::getValue)
                    .findFirst();
        }

        return Optional.empty();
    }

    /**
     * Access Token 쿠키를 조회합니다.
     *
     * @param request HTTP 요청 객체
     * @return Access Token 값 (Optional)
     */
    public Optional<String> getAccessToken(HttpServletRequest request) {
        return getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    /**
     * Refresh Token 쿠키를 조회합니다.
     *
     * @param request HTTP 요청 객체
     * @return Refresh Token 값 (Optional)
     */
    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookie(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * 쿠키를 삭제합니다.
     *
     * @param name     쿠키 이름
     * @param response HTTP 응답 객체
     */
    public void deleteCookie(String name, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)  // 즉시 만료
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Access Token 쿠키를 삭제합니다.
     *
     * @param response HTTP 응답 객체
     */
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        deleteCookie(ACCESS_TOKEN_COOKIE_NAME, response);
    }

    /**
     * Refresh Token 쿠키를 삭제합니다.
     *
     * @param response HTTP 응답 객체
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(REFRESH_TOKEN_COOKIE_NAME, response);
    }

    /**
     * 모든 인증 관련 쿠키를 삭제합니다. (로그아웃 시 사용)
     *
     * @param response HTTP 응답 객체
     */
    public void deleteAllAuthCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
    }
}