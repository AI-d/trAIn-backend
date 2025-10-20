package com.aid.train.backend.domain.user.entity;

import com.aid.train.backend.domain.terms.entity.UserConsent;
import com.aid.train.backend.domain.user.enums.JobType;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 엔티티
 * <p>
 * 로컬 회원가입 및 소셜 로그인 사용자 정보를 저장합니다.
 * <p>
 * 주요 기능:
 * - 로컬 회원가입 (이메일/비밀번호) 및 이메일 인증
 * - 소셜 로그인 (카카오/구글/네이버)
 * - 소셜 계정 연동 (하나의 User에 여러 소셜 계정 연동 가능)
 * - 닉네임 및 직업 정보 관리
 * - 계정 상태 관리 (ACTIVE, SUSPENDED)
 * <p>
 * 이메일 중복 정책:
 * - 같은 provider 내에서는 이메일 중복 불가
 * - 다른 provider 간에는 같은 이메일 사용 가능
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_provider",
                        columnNames = {"email", "primary_provider"}
                )
        },
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_status", columnList = "status"),
                @Index(name = "idx_user_last_login", columnList = "last_login_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * 사용자 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이메일 주소 (로그인 ID)
     * 같은 provider 내에서는 중복 불가
     */
    @Column(nullable = false, length = 100)
    private String email;

    /**
     * 비밀번호 (BCrypt 암호화, 60자)
     * LOCAL 계정만 사용, 소셜 계정은 null
     */
    @Column(length = 60)
    private String password;

    /**
     * 이름 (필수)
     * 서비스 내 표시명
     * 로컬 가입 시: 사용자 입력
     * 소셜 가입 시: 소셜에서 받은 이름
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 생년월일
     */
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /**
     * 직업 유형
     * STUDENT, EMPLOYEE, FREELANCER, JOB_SEEKER, OTHER
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType;

    /**
     * 직업 상세 정보
     * jobType이 OTHER인 경우 필수
     */
    @Column(name = "job_detail", length = 20)
    private String jobDetail;

    /**
     * 주 로그인 제공자
     * 최초 가입 시 사용한 방법 (LOCAL, GOOGLE, KAKAO, NAVER)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_provider", nullable = false, length = 20)
    private Provider primaryProvider;

    /**
     * 이메일 인증 완료 여부
     * LOCAL 계정은 false로 시작, 소셜 계정은 true
     */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * 계정 상태
     * ACTIVE: 정상, SUSPENDED: 정지
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 마지막 로그인 일시
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 연동된 소셜 계정 목록
     * 하나의 User에 여러 소셜 계정을 연동할 수 있습니다.
     * 예: LOCAL 계정에 Google, Kakao 연동 가능
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    /**
     * 약관 동의 내역
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
     * 계정 탈퇴 일시
     * null: 활성 계정
     * 값 있음: 탈퇴 계정 (soft delete)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 엔티티 저장 전 검증
     * LOCAL 계정은 비밀번호 필수
     */
    @PrePersist
    private void validateOnCreate() {
        if (this.primaryProvider == Provider.LOCAL) {
            if (this.password == null || this.password.trim().isEmpty()) {
                throw new IllegalStateException("LOCAL 계정은 비밀번호가 필수입니다.");
            }
        }
    }

    /**
     * 엔티티 수정 전 검증
     * 비밀번호는 빈 문자열 불가
     */
    @PreUpdate
    private void validateOnUpdate() {
        if (this.primaryProvider == Provider.LOCAL &&
                this.password != null && this.password.trim().isEmpty()) {
            throw new IllegalStateException("비밀번호는 빈 문자열일 수 없습니다.");
        }
    }

    /**
     * 로컬 계정 생성
     *
     * @param email     이메일 주소
     * @param password  BCrypt 암호화된 비밀번호
     * @param name      이름
     * @param birthDate 생년월일
     * @param jobType   직업 유형
     * @return User 엔티티
     */
    public static User createLocalUser(String email, String password, String name, LocalDate birthDate, JobType jobType) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .birthDate(birthDate)
                .jobType(jobType)
                .primaryProvider(Provider.LOCAL)
                .emailVerified(false)
                .build();
    }

    /**
     * 소셜 계정 생성
     *
     * @param email     소셜에서 받은 이메일
     * @param name      소셜에서 받은 이름
     * @param birthDate 생년월일
     * @param jobType   직업 유형
     * @param provider  소셜 제공자 (GOOGLE, KAKAO, NAVER)
     * @return User 엔티티
     */
    public static User createSocialUser(String email, String name, LocalDate birthDate, JobType jobType, Provider provider) {
        return User.builder()
                .email(email)
                .name(name)
                .birthDate(birthDate)
                .jobType(jobType)
                .primaryProvider(provider)
                .emailVerified(true)
                .build();
    }

    /**
     * 이메일 인증 완료
     */
    public void verifyEmail() {
        this.emailVerified = true;
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 계정 상태 변경 (ACTIVE <-> SUSPENDED)
     */
    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * 비밀번호 변경
     * LOCAL 계정만 가능
     *
     * @param encodedPassword BCrypt 암호화된 새 비밀번호
     * @throws IllegalStateException 소셜 계정인 경우
     */
    public void updatePassword(String encodedPassword) {
        if (this.primaryProvider != Provider.LOCAL) {
            throw new IllegalStateException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        }
        this.password = encodedPassword;
    }

    /**
     * 이름 변경
     *
     * @param name 새 닉네임
     */
    public void updateName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
    }

    /**
     * 생년월일 변경
     *
     * @param birthDate 새 생년월일
     */
    public void updateBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * 직업 정보 업데이트
     *
     * @param jobType   직업 유형
     * @param jobDetail 직업 상세 (OTHER인 경우 필수)
     */
    public void updateJobInfo(JobType jobType, String jobDetail) {
        this.jobType = jobType;
        if (jobType != null && jobType.requiresDetail() && jobDetail != null) {
            this.jobDetail = jobDetail.trim();
        } else {
            this.jobDetail = null;
        }
    }

    /**
     * LOCAL 계정 여부 확인
     *
     * @return LOCAL이면 true
     */
    public boolean isLocalAccount() {
        return this.primaryProvider == Provider.LOCAL;
    }

    /**
     * 소셜 계정 여부 확인
     *
     * @return 소셜이면 true
     */
    public boolean isSocialAccount() {
        return this.primaryProvider != Provider.LOCAL;
    }

    /**
     * 이메일 인증 완료 여부 확인
     *
     * @return 인증 완료면 true
     */
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(this.emailVerified);
    }

    /**
     * 활성 계정 여부 확인
     *
     * @return ACTIVE 상태면 true
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * 만 나이 계산
     *
     * @return 만 나이
     */
    public int getAge() {
        if (this.birthDate == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        int age = today.getYear() - this.birthDate.getYear();
        if (today.getMonthValue() < this.birthDate.getMonthValue() ||
                (today.getMonthValue() == this.birthDate.getMonthValue() &&
                        today.getDayOfMonth() < this.birthDate.getDayOfMonth())) {
            age--;
        }
        return age;
    }

    /**
     * 탈퇴 여부 확인
     *
     * @return 탈퇴 계정이면 true
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 계정 탈퇴 (soft delete)
     * <p>
     * 탈퇴 시 처리:
     * - deletedAt 설정
     * - status를 SUSPENDED로 변경
     * - 연관된 RefreshToken은 Service에서 삭제
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = UserStatus.SUSPENDED;
    }

    /**
     * 계정 복구 (관리자 기능)
     * <p>
     * 탈퇴 취소 시:
     * - deletedAt을 null로 변경
     * - status를 ACTIVE로 변경
     */
    public void restore() {
        this.deletedAt = null;
        this.status = UserStatus.ACTIVE;
    }
}