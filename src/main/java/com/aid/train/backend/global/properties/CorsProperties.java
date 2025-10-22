package com.aid.train.backend.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CORS(Cross-Origin Resource Sharing) 관련 설정을 application.yml 파일로부터 바인딩하는 클래스입니다.
 * 이 클래스는 'cors' prefix로 시작하는 속성들을 관리하며,
 * 다른 도메인에서의 API 요청을 허용하기 위한 정책을 정의합니다.
 *
 * @author 왕택준
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * 요청을 허용할 출처(Origin) 목록.
     * 쉼표(,)로 여러 출처를 구분합니다. (예: "http://localhost:5050,https://your-frontend.com")
     */
    private String allowedOrigins;

    /**
     * 허용할 HTTP 메서드 목록.
     * 쉼표(,)로 구분합니다. (예: "GET,POST,PUT,DELETE")
     */
    private String allowedMethods;

    /**
     * 요청에서 허용할 헤더 목록.
     * '*'는 모든 헤더를 허용함을 의미합니다.
     */
    private String allowedHeaders;

    /**
     * 자격 증명(credentials) 정보 (예: 쿠키, 인증 헤더)를 포함한 요청을 허용할지 여부.
     */
    private boolean allowCredentials;

    /**
     * Pre-flight 요청의 결과를 캐시할 시간 (초 단위).
     * 이 시간 동안 브라우저는 동일한 Pre-flight 요청을 다시 보내지 않습니다.
     */
    private Long maxAge;
}