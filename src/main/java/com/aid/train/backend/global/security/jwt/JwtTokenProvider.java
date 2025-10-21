package com.aid.train.backend.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider 클래스입니다.
 * Access, Refresh, Email Verify, Password Reset 등 다양한 토큰 타입을 지원합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    /**
     * Bean 초기화 후 SecretKey 생성
     */
    @PostConstruct
    void initKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.warn("JWT secret 길이가 256bit(32바이트) 미만입니다. 운영에서는 더 긴 키를 권장합니다.");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ====================== 토큰 생성 메서드들 ======================

    /**
     * Access Token을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param email  사용자 이메일
     * @return JWT Access Token
     */
    public String generateAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("tokenType", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.getAccessTokenValidity())))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Refresh Token을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return JWT Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("tokenType", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.getRefreshTokenValidity())))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 이메일 인증용 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param email  이메일
     * @return JWT Email Verify Token
     */
    public String generateEmailVerifyToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("tokenType", "EMAIL_VERIFY")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(1800000))) // 30분
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 비밀번호 재설정용 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param email  이메일
     * @return JWT Password Reset Token
     */
    public String generatePasswordResetToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("tokenType", "PASSWORD_RESET")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(3600000))) // 1시간
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // ====================== 토큰 파싱 및 검증 메서드들 ======================

    /**
     * JWT 토큰 파싱 및 Claims 추출
     * ±60초 시계 오차를 허용합니다.
     *
     * @param token 파싱할 JWT 토큰
     * @return 토큰의 Claims 객체
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .clockSkewSeconds(60)  // ±60초 시계 오차 허용
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.warn("JWT bad signature");
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT illegal argument");
            throw e;
        }
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * JWT 토큰에서 이메일을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("validateToken failed: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Access Token 타입 검증
     *
     * @param token 검증할 토큰
     * @return Access Token이면 true
     */
    public boolean isAccessToken(String token) {
        return "ACCESS".equals(String.valueOf(parseClaims(token).get("tokenType")));
    }

    /**
     * Refresh Token 타입 검증
     *
     * @param token 검증할 토큰
     * @return Refresh Token이면 true
     */
    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(String.valueOf(parseClaims(token).get("tokenType")));
    }

    /**
     * Email Verify Token 타입 검증
     *
     * @param token 검증할 토큰
     * @return Email Verify Token이면 true
     */
    public boolean isEmailVerifyToken(String token) {
        return "EMAIL_VERIFY".equals(String.valueOf(parseClaims(token).get("tokenType")));
    }

    /**
     * Password Reset Token 타입 검증
     *
     * @param token 검증할 토큰
     * @return Password Reset Token이면 true
     */
    public boolean isPasswordResetToken(String token) {
        return "PASSWORD_RESET".equals(String.valueOf(parseClaims(token).get("tokenType")));
    }

    /**
     * JWT 토큰의 만료 시간을 반환합니다.
     *
     * @param token JWT 토큰
     * @return 만료 시간
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration();
    }

    /**
     * Access Token 만료 시간을 초 단위로 반환
     *
     * @return Access Token 만료 시간 (초)
     */
    public int getAccessMaxAge() {
        return Math.toIntExact(jwtProperties.getAccessTokenValidity() / 1000L);
    }

    /**
     * Refresh Token 만료 시간을 초 단위로 반환
     *
     * @return Refresh Token 만료 시간 (초)
     */
    public int getRefreshMaxAge() {
        return Math.toIntExact(jwtProperties.getRefreshTokenValidity() / 1000L);
    }
}