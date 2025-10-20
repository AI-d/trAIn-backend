package com.aid.train.backend.global.security.service;

import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security의 UserDetailsService를 구현한 클래스입니다.
 * <p>
 * JWT 인증 시에는 토큰에서 추출한 사용자 ID를 기반으로,
 * 일반적인 Username/Password 인증 시에는 이메일을 기반으로 DB에서 사용자 정보를 조회합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일(username)을 기반으로 사용자 정보를 조회합니다.
     * <p>
     * 이 메서드는 Spring Security의 기본 인증 메커니즘에서 사용됩니다.
     * 우리 시스템에서는 주로 JWT 인증을 사용하므로, 이 메서드가 직접 호출되는 경우는 드뭅니다.
     * (예: UsernamePasswordAuthenticationFilter를 직접 사용하는 경우)
     * </p>
     *
     * @param email 사용자의 이메일 주소 (Spring Security의 'username')
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 해당 이메일의 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일 기반 로그인 시에는 로컬 가입자만 대상으로 함
        User user = userRepository.findByEmailAndPrimaryProvider(email, com.aid.train.backend.domain.user.enums.Provider.LOCAL)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return new CustomUserDetails(user);
    }

    /**
     * 사용자 ID를 기반으로 UserDetails 객체를 로드합니다.
     * <p>
     * 이 메서드는 {@code JwtAuthenticationFilter}에서 JWT 토큰 검증 후 호출되어,
     * 토큰에 포함된 사용자 ID를 통해 실제 사용자 정보를 DB에서 조회하는 데 사용됩니다.
     * </p>
     *
     * @param userId 조회할 사용자의 ID
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 해당 ID의 사용자를 찾을 수 없는 경우
     */
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: ID " + userId));

        return new CustomUserDetails(user);
    }
}