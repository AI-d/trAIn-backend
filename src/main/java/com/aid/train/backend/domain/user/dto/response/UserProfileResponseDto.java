package com.aid.train.backend.domain.user.dto.response;

import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.JobType;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 프로필 응답 DTO입니다.
 * 사용자의 상세 정보를 클라이언트에 전달합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 프로필 응답")
public class UserProfileResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "생년월일", example = "1998-08-07")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(description = "만 나이", example = "26")
    private Integer age;

    @Schema(description = "직업 유형", example = "EMPLOYEE")
    private JobType jobType;

    @Schema(description = "기타 직업 상세", example = "프리랜서 개발자")
    private String jobDetail;

    @Schema(description = "주 인증 제공자", example = "LOCAL")
    private Provider primaryProvider;

    @Schema(description = "이메일 인증 완료 여부", example = "true")
    private Boolean emailVerified;

    @Schema(description = "계정 상태", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "마지막 로그인 시간", example = "2025-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @Schema(description = "계정 생성 일시", example = "2025-01-01T09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "계정 수정 일시", example = "2025-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * User 엔티티로부터 DTO를 생성합니다.
     *
     * @param user User 엔티티
     * @return UserProfileResponseDto
     */
    public static UserProfileResponseDto from(User user) {
        return UserProfileResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .age(user.getAge())
                .jobType(user.getJobType())
                .jobDetail(user.getJobDetail())
                .primaryProvider(user.getPrimaryProvider())
                .emailVerified(user.getEmailVerified())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}