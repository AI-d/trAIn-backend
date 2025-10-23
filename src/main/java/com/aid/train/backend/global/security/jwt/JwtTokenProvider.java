package com.aid.train.backend.global.security.jwt;

import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.properties.JwtProperties;
import com.aid.train.backend.global.util.LogMaskingUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT(Json Web Token) 생성 및 검증을 담당하는 유틸리티 클래스입니다.
 * <p>
 * Access, Refresh, EmailVerification, SocialSignupPending 등 다양한 용도의 토큰을 생성하고,
 * 서명 검증, 만료 여부 확인, 클레임 추출 등의 기능을 제공합니다.
 * </p>
 *
 * @author 왕택준
 * @see JwtProperties
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // ===== JWT 클레임(Claim) 상수 =====
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String CLAIM_PROVIDER = "provider";
    private static final String CLAIM_PROVIDER_ID = "providerId";
    private static final String CLAIM_NAME = "name";

    // ===== JWT 토큰 타입 상수 =====
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";
    private static final String TYPE_EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    private static final String TYPE_SOCIAL_SIGNUP_PENDING = "SOCIAL_SIGNUP_PENDING";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ===== 토큰 생성 메서드 =====

    /**
     * Access Token과 Refresh Token을 함께 생성합니다.
     *
     * @param userId 사용자 ID
     * @param email  사용자 이메일
     * @return 생성된 토큰 정보를 담은 JwtResponse 객체
     */
    public JwtResponse generateTokens(Long userId, String email) {
        String accessToken = generateAccessToken(userId, email);
        String refreshToken = generateRefreshToken(userId, email);
        return new JwtResponse(accessToken, refreshToken);
    }

    /**
     * Access Token (15분)을 생성합니다. API 인증에 사용됩니다.
     *
     * @param userId 사용자 ID
     * @param email  사용자 이메일
     * @return 생성된 Access Token
     */
    public String generateAccessToken(Long userId, String email) {
        return generateToken(userId, email, TYPE_ACCESS, jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Refresh Token (14일)을 생성합니다. Access Token 갱신에 사용됩니다.
     *
     * @param userId 사용자 ID
     * @param email  사용자 이메일
     * @return 생성된 Refresh Token
     */
    public String generateRefreshToken(Long userId, String email) {
        return generateToken(userId, email, TYPE_REFRESH, jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * 이메일 인증 토큰 (15분)을 생성합니다. 로컬 회원가입 시 이메일 인증 절차에 사용됩니다.
     *
     * @param userId 사용자 ID
     * @param email  인증할 이메일
     * @return 생성된 Email Verification Token
     */
    public String generateEmailVerificationToken(Long userId, String email) {
        return generateToken(userId, email, TYPE_EMAIL_VERIFICATION, jwtProperties.getAccessTokenExpiration());
    }

    /**
     * 소셜 회원가입 대기 토큰 (15분)을 생성합니다. 신규 소셜 사용자의 추가 정보 입력을 위해 사용됩니다.
     *
     * @param provider   소셜 제공자 이름 (e.g., "GOOGLE")
     * @param providerId 소셜 플랫폼 사용자 ID
     * @param email      소셜 이메일
     * @param name       소셜 사용자 이름
     * @return 생성된 Social Signup Pending Token
     */
    public String generateSocialSignupPendingToken(String provider, String providerId, String email, String name) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration()); // Access Token과 동일한 15분 만료

        return Jwts.builder()
                .claim(CLAIM_PROVIDER, provider)
                .claim(CLAIM_PROVIDER_ID, providerId)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_NAME, name)
                .claim(CLAIM_TOKEN_TYPE, TYPE_SOCIAL_SIGNUP_PENDING)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // ===== 토큰 검증 메서드 =====

    /**
     * 토큰의 유효성을 검증합니다. 서명, 만료 시간, 형식 등을 확인합니다.
     *
     * @param token 검증할 JWT
     * @return 유효하면 true
     * @throws TrainException 유효하지 않은 경우 (서명 오류, 만료, 형식 오류 등)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다. tokenPrefix={}...", LogMaskingUtil.maskToken(token), e);
            throw new TrainException(ErrorCode.TOKEN_INVALID_SIGNATURE);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다. tokenPrefix={}...", LogMaskingUtil.maskToken(token), e);
            throw new TrainException(ErrorCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다. tokenPrefix={}...", LogMaskingUtil.maskToken(token), e);
            throw new TrainException(ErrorCode.TOKEN_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다. tokenPrefix={}...", LogMaskingUtil.maskToken(token), e);
            throw new TrainException(ErrorCode.TOKEN_INVALID);
        }
    }

    // ===== 클레임 추출 메서드 =====

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     */
    public Long getUserIdFromToken(String token) {
        return getClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    /**
     * 토큰에서 이메일을 추출합니다.
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).get(CLAIM_EMAIL, String.class);
    }

    /**
     * 토큰에서 소셜 제공자 이름을 추출합니다.
     */
    public String getProviderFromToken(String token) {
        return getClaims(token).get(CLAIM_PROVIDER, String.class);
    }

    /**
     * 토큰에서 소셜 제공자 ID를 추출합니다.
     */
    public String getProviderIdFromToken(String token) {
        return getClaims(token).get(CLAIM_PROVIDER_ID, String.class);
    }

    /**
     * 토큰에서 사용자 이름을 추출합니다.
     */
    public String getNameFromToken(String token) {
        return getClaims(token).get(CLAIM_NAME, String.class);
    }

    /**
     * 토큰에서 만료 일시를 LocalDateTime으로 추출합니다.
     */
    public LocalDateTime getExpiryDateTimeFromToken(String token) {
        Date expiryDate = getClaims(token).getExpiration();
        return Instant.ofEpochMilli(expiryDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 토큰에서 모든 클레임을 추출합니다.
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ===== 내부 토큰 생성 로직 =====

    private String generateToken(Long userId, String email, String tokenType, long validityInMilliseconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // ===== 내부 DTO 클래스 =====

    /**
     * Access Token과 Refresh Token을 담는 불변 데이터 객체(DTO)입니다.
     * Java Record를 사용하여 생성자, Getter, equals, hashCode, toString을 자동 생성합니다.
     *
     * @param accessToken  발급된 Access Token
     * @param refreshToken 발급된 Refresh Token
     */
    public record JwtResponse(String accessToken, String refreshToken) {
    }
}