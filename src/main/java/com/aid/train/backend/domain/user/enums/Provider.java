package com.aid.train.backend.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 인증 제공자 Enum 클래스입니다.
 * 사용자 인증 방식을 구분합니다.
 *
 * <p>
 * 사용 위치:
 * <ul>
 *   <li>User.primaryProvider: 사용자의 주 인증 방식</li>
 *   <li>SocialAccount.provider: 연동된 소셜 계정의 제공자</li>
 *   <li>PendingSocialUser.provider: 임시 저장된 소셜 계정의 제공자</li>
 * </ul>
 * </p>
 *
 * <p>
 * 비즈니스 규칙:
 * <ul>
 *   <li>LOCAL: 이메일/비밀번호 기반 로컬 회원가입</li>
 *   <li>KAKAO, GOOGLE, NAVER: OAuth 2.0 소셜 로그인</li>
 *   <li>PRIMARY 제공자가 LOCAL이면 비밀번호 필수</li>
 *   <li>PRIMARY 제공자가 소셜이면 비밀번호 null</li>
 * </ul>
 * </p>
 *
 * <p>
 * OAuth2 연동:
 * <ul>
 *   <li>registrationId: Spring Security OAuth2의 클라이언트 등록 ID와 1:1 매핑</li>
 *   <li>fromRegistrationId(): 외부 입력(대소문자/공백 무관)을 정규화하여 안전하게 변환</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum Provider {

    /**
     * 로컬 회원가입 (이메일/비밀번호)
     */
    LOCAL("로컬", "email", "local"),

    /**
     * 카카오 소셜 로그인
     */
    KAKAO("카카오", "https://kauth.kakao.com", "kakao"),

    /**
     * 구글 소셜 로그인
     */
    GOOGLE("구글", "https://accounts.google.com", "google"),

    /**
     * 네이버 소셜 로그인
     */
    NAVER("네이버", "https://nid.naver.com", "naver");

    private final String displayName;
    private final String authUrl;
    private final String registrationId;

    /**
     * OAuth2 registrationId로 Provider를 조회합니다.
     * 대소문자 및 공백을 무시하고 안전하게 변환합니다.
     *
     * @param registrationId OAuth2 클라이언트 등록 ID (예: "google", "kakao")
     * @return 해당하는 Provider
     * @throws IllegalArgumentException registrationId가 null이거나 지원하지 않는 제공자인 경우
     */
    public static Provider fromRegistrationId(String registrationId) {
        if (registrationId == null) {
            throw new IllegalArgumentException("registrationId는 null일 수 없습니다.");
        }

        String normalized = registrationId.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(provider -> provider.registrationId.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 인증 제공자입니다: " + registrationId));
    }

    /**
     * 로컬 회원가입 방식인지 확인합니다.
     *
     * @return 로컬 회원가입이면 true, 아니면 false
     */
    public boolean isLocal() {
        return this == LOCAL;
    }

    /**
     * 소셜 로그인 방식인지 확인합니다.
     *
     * @return 소셜 로그인이면 true, 아니면 false
     */
    public boolean isSocial() {
        return this != LOCAL;
    }
}