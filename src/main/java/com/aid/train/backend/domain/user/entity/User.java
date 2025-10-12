package com.aid.train.backend.domain.user.entity;

import com.aid.train.backend.domain.terms.entity.UserConsent;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 엔티티 클래스입니다.
 * 로컬 회원가입 및 소셜 로그인 사용자 정보를 저장합니다.
 *
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>로컬 회원가입 사용자 관리 (이메일/비밀번호)</li>
 *   <li>소셜 로그인 사용자 관리 (카카오/구글/네이버)</li>
 *   <li>이메일 인증 상태 관리</li>
 *   <li>계정 상태 관리 (활성/비활성/정지/탈퇴)</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * 사용자 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이메일 주소 (필수, 고유값)
     * 로컬 회원가입 및 소셜 로그인 식별자로 사용
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 비밀번호 (암호화 저장, BCrypt 60자)
     * 로컬 회원가입 사용자만 사용, 소셜 로그인 사용자는 null
     */
    @Column(length = 60)
    private String password;

    /**
     * 사용자 이름 (필수, 최대 50자)
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 주 인증 제공자 (LOCAL, KAKAO, GOOGLE, NAVER)
     * 최초 가입 시 사용한 인증 방식
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider primaryProvider;

    /**
     * 이메일 인증 완료 여부
     * 로컬 회원가입 사용자는 이메일 인증 필수
     * 소셜 로그인 사용자는 기본값 true
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * 계정 상태 (ACTIVE, INACTIVE, SUSPENDED, WITHDRAWN)
     * 기본값: ACTIVE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 회원 탈퇴 일시
     * 탈퇴 시에만 값이 설정됨
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 연동된 소셜 계정 목록
     * 하나의 계정에 여러 소셜 로그인 연동 가능
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    /**
     * 사용자가 동의한 약관 목록
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore
    private List<UserConsent> consents = new ArrayList<>();

    /**
     * 계정 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 계정 정보 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 엔티티 저장/수정 전 비밀번호 무결성을 검증합니다.
     * LOCAL 계정은 비밀번호 필수, 소셜 로그인 계정은 비밀번호 null 처리
     *
     * @throws IllegalStateException LOCAL 계정인데 비밀번호가 없는 경우
     */
    @PrePersist
    @PreUpdate
    private void validatePasswordIntegrity() {
        if (this.primaryProvider == Provider.LOCAL) {
            if (this.password == null || this.password.trim().isEmpty()) {
                throw new IllegalStateException("LOCAL 계정은 비밀번호가 필수입니다.");
            }
        }
        if (this.primaryProvider != Provider.LOCAL) {
            this.password = null;
        }
    }

    /**
     * 이메일 인증을 완료 처리합니다.
     */
    public void verifyEmail() {
        this.emailVerified = true;
    }

    /**
     * 계정 상태를 변경합니다.
     *
     * @param status 변경할 계정 상태
     */
    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * 비밀번호를 변경합니다.
     * LOCAL 계정만 사용 가능
     *
     * @param encodedPassword 암호화된 새 비밀번호
     * @throws IllegalStateException 소셜 로그인 계정인 경우
     */
    public void updatePassword(String encodedPassword) {
        if (this.primaryProvider != Provider.LOCAL) {
            throw new IllegalStateException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        }
        this.password = encodedPassword;
    }

    /**
     * 회원 탈퇴를 처리합니다.
     * 계정 상태를 WITHDRAWN으로 변경하고 탈퇴 일시를 기록합니다.
     */
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 휴면 계정으로 전환합니다.
     * 1년 이상 미접속 계정을 INACTIVE 상태로 변경합니다.
     * DataCleanupScheduler에서 호출됩니다.
     */
    public void convertToInactive() {
        this.status = UserStatus.INACTIVE;
    }

    /**
     * LOCAL 계정 여부를 확인합니다.
     *
     * @return LOCAL 계정이면 true, 아니면 false
     */
    public boolean isLocalAccount() {
        return this.primaryProvider == Provider.LOCAL;
    }

    /**
     * 소셜 로그인 계정 여부를 확인합니다.
     *
     * @return 소셜 로그인 계정이면 true, 아니면 false
     */
    public boolean isSocialAccount() {
        return this.primaryProvider != Provider.LOCAL;
    }
}