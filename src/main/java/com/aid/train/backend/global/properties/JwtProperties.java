package com.aid.train.backend.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT(Json Web Token) 관련 설정을 application.yml 파일로부터 바인딩하는 클래스입니다.
 * 이 클래스는 'jwt' prefix로 시작하는 속성들을 관리합니다.
 *
 * @author 왕택준
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 서명 및 검증에 사용될 비밀 키 (Base64 인코딩).
     * 이 값은 외부에 노출되어서는 안 됩니다.
     */
    private String secret;

    /**
     * Access Token의 만료 시간 (밀리초 단위).
     * 예: 900000 (15분)
     */
    private Long accessTokenExpiration;

    /**
     * Refresh Token의 만료 시간 (밀리초 단위).
     * 예: 1209600000 (14일)
     */
    private Long refreshTokenExpiration;
}