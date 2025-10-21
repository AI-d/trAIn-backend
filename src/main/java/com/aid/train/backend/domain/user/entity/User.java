package com.aid.train.backend.domain.user.entity;

import com.aid.train.backend.domain.terms.entity.UserConsent;
import com.aid.train.backend.domain.user.enums.JobType;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
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
 *   <li>이메일 인증 상태 관리 (EmailVerification 엔티티 참조)</li>
 *   <li>계정 상태 관리 (활성/비활성/정지/탈퇴)</li>
 *   <li>개인 정보 관리 (생년월일, 직업)</li>
 * </ul>
 * </p>
 *
 * <p>
 * 이메일 중복 정책:
 * <ul>
 *   <li>같은 provider 내에서는 이메일 중복 불가</li>
 *   <li>다른 provider 간에는 같은 이메일 사용 가능</li>
 *   <li>예: test@gmail.com + LOCAL, test@gmail.com + GOOGLE 모두 가능</li>
 * </ul>
 * </p>
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 60)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType;

    @Column(name = "job_detail", length = 100)
    private String jobDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_provider", nullable = false, length = 20)
    private Provider primaryProvider;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore
    private List<UserConsent> consents = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void validateOnCreate() {
        if (this.primaryProvider == Provider.LOCAL) {
            if (this.password == null || this.password.trim().isEmpty()) {
                throw new IllegalStateException("LOCAL 계정은 비밀번호가 필수입니다.");
            }
        }
    }

    @PreUpdate
    private void validateOnUpdate() {
        if (this.primaryProvider == Provider.LOCAL &&
                this.password != null && this.password.trim().isEmpty()) {
            throw new IllegalStateException("비밀번호는 빈 문자열일 수 없습니다.");
        }
    }

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

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    public void updatePassword(String encodedPassword) {
        if (this.primaryProvider != Provider.LOCAL) {
            throw new IllegalStateException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        }
        this.password = encodedPassword;
    }

    public void updatePersonalInfo(String name, LocalDate birthDate) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        this.birthDate = birthDate;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateJobInfo(JobType jobType, String jobDetail) {
        this.jobType = jobType;
        if (jobType != null && jobType.requiresDetail() && jobDetail != null) {
            this.jobDetail = jobDetail.trim();
        } else {
            this.jobDetail = null;
        }
    }

    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.status = UserStatus.ACTIVE;
        this.deletedAt = null;
    }

    public void convertToInactive() {
        this.status = UserStatus.INACTIVE;
    }

    public boolean isLocalAccount() {
        return this.primaryProvider == Provider.LOCAL;
    }

    public boolean isSocialAccount() {
        return this.primaryProvider != Provider.LOCAL;
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(this.emailVerified);
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean canRestore() {
        if (this.status != UserStatus.WITHDRAWN || this.deletedAt == null) {
            return false;
        }
        LocalDateTime restoreDeadline = this.deletedAt.plusDays(30);
        return LocalDateTime.now().isBefore(restoreDeadline);
    }

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
}
