package com.aid.train.backend.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HTTP 쿠키 관련 설정을 application.yml 파일로부터 바인딩하는 클래스입니다.
 * 이 클래스는 'cookie' prefix로 시작하는 속성들을 관리하며,
 * 주로 Refresh Token을 안전하게 저장하는 데 사용됩니다.
 *
 * @author 왕택준
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cookie")
public class CookieProperties {

    /**
     * Access Token을 저장할 쿠키의 이름.
     */
    private String accessTokenName;

    /**
     * Refresh Token을 저장할 쿠키의 이름.
     */
    private String refreshTokenName;

    /**
     * 쿠키가 적용될 도메인.
     * 로컬 환경에서는 'localhost', 프로덕션 환경에서는 '.yourdomain.com' 과 같이 설정됩니다.
     */
    private String domain;

    /**
     * 쿠키가 유효한 경로.
     * '/'로 설정 시, 전체 애플리케이션 경로에서 쿠키가 유효합니다.
     */
    private String path;

    /**
     * Access Token 쿠키의 최대 유효 시간 (초 단위).
     */
    private Integer accessTokenMaxAge;

    /**
     * Refresh Token 쿠키의 최대 유효 시간 (초 단위).
     * 예: 1209600 (14일)
     */
    private Integer refreshTokenMaxAge;

    /**
     * 쿠키를 HTTPS 프로토콜에서만 전송할지 여부.
     * 프로덕션 환경에서는 true로 설정하는 것이 안전합니다.
     */
    private boolean secure;

    /**
     * Cross-Site 요청에 대한 쿠키 전송 정책.
     * 'Strict', 'Lax', 'None' 중 하나로 설정됩니다.
     */
    private String sameSite;

    /**
     * JavaScript를 통해 쿠키에 접근하는 것을 방지할지 여부.
     * true로 설정 시, XSS(Cross-Site Scripting) 공격을 방어하는 데 도움이 됩니다.
     */
    private boolean httpOnly;
}