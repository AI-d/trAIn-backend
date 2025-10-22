package com.aid.train.backend.global.config;

import com.aid.train.backend.global.security.handler.JwtAccessDeniedHandler;
import com.aid.train.backend.global.security.handler.JwtAuthenticationEntryPoint;
import com.aid.train.backend.global.security.handler.OAuth2AuthenticationSuccessHandler;
import com.aid.train.backend.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 전체 설정 클래스입니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize 등 메서드 수준 보안 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // --- Public API: 인증 없이 접근 허용 ---
                        .requestMatchers(
                                // 정적 리소스
                                "/favicon.ico",
                                "/css/**", "/js/**", "/images/**",
                                "/.well-known/**",
                                "/error",

                                // 인증/회원가입
                                "/api/v1/users/signup",
                                "/api/v1/users/login",
                                "/api/v1/users/refresh",

                                // 이메일 인증
                                "/api/v1/verification/email",
                                "/api/v1/verification/email/resend",

                                // 약관 조회
                                "/api/v1/terms",

                                // 소셜 로그인
                                "/oauth2/**",
                                "/login/oauth2/**",

                                // API 문서 (모든 Swagger 경로)
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/webjars/**",

                                // Actuator (헬스체크 등)
                                "/actuator/**",

                                // 테스트용 엔드포인트 허용 (필요시 제거)
                                "/test/",
                                // WebSocket 전체 허용
                                "/ws/", "/ws/**",
                                // 인증 API 허용
                                "/api/**"

                        ).permitAll()
                        // --- Authenticated API: 인증된 사용자만 접근 허용 ---
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // STATELESS 환경에서 OAuth2의 state 파라미터 검증을 위해
                        // 일시적으로 세션을 사용하는 Request Repository를 설정합니다.
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestRepository(new HttpSessionOAuth2AuthorizationRequestRepository())
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}