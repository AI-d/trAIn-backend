package com.aid.train.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (WebSocket 사용 시 필수)
                .csrf(AbstractHttpConfigurer::disable)

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/api/health").permitAll() // 임시: 2025.10.10 배포 테스트용, 추후 삭제 예정
                        .requestMatchers("/ws/**").permitAll()          // WebSocket 전체 허용
                        .requestMatchers("/api/auth/**").permitAll()    // 인증 API 허용
                        .anyRequest().authenticated()                   // 나머지는 인증 필요
                );

        return http.build();
    }
}
