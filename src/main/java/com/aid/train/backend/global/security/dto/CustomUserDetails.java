package com.aid.train.backend.global.security.dto;

import com.aid.train.backend.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Spring Security에서 사용하는 UserDetails의 커스텀 구현체입니다.
 * <p>
 * User 엔티티를 감싸서 인증 및 인가에 필요한 정보를 제공합니다.
 * OAuth2User 인터페이스도 함께 구현하여 소셜 로그인 시에도 일관된 사용자 객체를 사용합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final User user;
    private Map<String, Object> attributes;

    /**
     * 일반 JWT 인증용 생성자
     *
     * @param user DB에서 조회한 User 엔티티
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * OAuth2 소셜 로그인용 생성자
     *
     * @param user       DB에서 조회 또는 생성한 User 엔티티
     * @param attributes OAuth2 제공자로부터 받은 사용자 속성 정보
     */
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    /**
     * 사용자의 ID (PK)를 반환합니다.
     *
     * @return 사용자 ID
     */
    public Long getUserId() {
        return user.getId();
    }

    // --- UserDetails 인터페이스 구현 ---

    /**
     * 사용자에게 부여된 권한 목록을 반환합니다.
     * <p>
     * TODO: 향후 User 엔티티에 Role 필드가 추가되면, 해당 Role을 기반으로 동적 권한을 부여하도록 수정해야 합니다.
     * </p>
     *
     * @return 권한 목록 (현재는 "ROLE_USER"로 고정)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 예: user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Spring Security에서 사용자를 식별하는 고유한 값으로 사용됩니다. (일반적으로 로그인 ID)
     *
     * @return 사용자 이메일
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 정책 없음
    }

    /**
     * 계정이 잠겨있는지 여부를 반환합니다.
     *
     * @return 계정이 정지(SUSPENDED) 또는 탈퇴(WITHDRAWN) 상태가 아니면 true
     */
    @Override
    public boolean isAccountNonLocked() {
        return !user.getStatus().isTerminated() && user.getStatus() != com.aid.train.backend.domain.user.enums.UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 정책 없음
    }

    /**
     * 계정이 활성화되어 있는지 여부를 반환합니다.
     *
     * @return 계정이 로그인 가능한 상태(ACTIVE, INACTIVE)이면 true
     */
    @Override
    public boolean isEnabled() {
        return user.getStatus().canLogin();
    }

    // --- OAuth2User 인터페이스 구현 ---

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        // OAuth2 명세에서 name은 사용자를 식별하는 고유 ID를 의미합니다.
        // 따라서 우리 시스템의 User ID를 문자열로 반환합니다.
        return String.valueOf(user.getId());
    }
}