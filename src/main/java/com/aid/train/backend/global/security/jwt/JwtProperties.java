package com.aid.train.backend.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private Long accessTokenValidity = 3600000L;  // 1시간
    private Long refreshTokenValidity = 604800000L;  // 7일
}