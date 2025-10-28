package com.aid.train.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 설정 클래스입니다.
 * <p>
 * BCrypt 알고리즘을 사용하여 비밀번호를 암호화합니다.
 * BCrypt는 단방향 해시 함수로 salt를 자동으로 생성하여 보안성이 높습니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt 해시 강도(코스트).
     * 기본값 12, 환경 변수로 조정 가능.
     */
    @Value("${security.password.bcrypt-strength:12}")
    private int bcryptStrength;

    /**
     * BCrypt 비밀번호 인코더를 빈으로 등록합니다.
     * <p>
     * BCrypt는 다음과 같은 특징이 있습니다:
     * <ul>
     *   <li>단방향 해시 함수 (복호화 불가능)</li>
     *   <li>자동 salt 생성 및 관리</li>
     *   <li>강도 조절 가능 (기본값: 12)</li>
     *   <li>Rainbow Table 공격 방어</li>
     * </ul>
     * </p>
     *
     * @return BCrypt 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
}
