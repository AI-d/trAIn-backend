package com.aid.train.backend.domain.user.dto.response;

import com.aid.train.backend.domain.user.enums.JobType;
import com.aid.train.backend.domain.user.enums.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 프로필 응답 DTO입니다.
 * <p>
 * 사용자 프로필 조회 및 수정 시 사용되는 응답 정보를 담습니다.
 * 로그인한 사용자 본인의 프로필만 조회 및 수정할 수 있습니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "사용자 프로필 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponseDto {

    /**
     * 사용자 ID
     * <p>
     * 사용자의 고유 식별자입니다.
     * </p>
     */
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    /**
     * 이메일 주소
     * <p>
     * 사용자의 이메일 주소입니다.
     * 이메일은 수정할 수 없습니다.
     * </p>
     */
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    /**
     * 사용자 이름
     * <p>
     * 사용자의 이름입니다.
     * 프로필 수정 시 변경 가능합니다.
     * </p>
     */
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    /**
     * 생년월일
     * <p>
     * 사용자의 생년월일입니다.
     * 프로필 수정 시 변경 가능합니다.
     * </p>
     */
    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthDate;

    /**
     * 직업 유형
     * <p>
     * 사용자의 직업 유형입니다.
     * 프로필 수정 시 변경 가능합니다.
     * </p>
     */
    @Schema(description = "직업 유형", example = "EMPLOYEE")
    private JobType jobType;

    /**
     * 직업 상세
     * <p>
     * 직업이 기타인 경우 입력한 직업 상세입니다.
     * 프로필 수정 시 변경 가능합니다.
     * </p>
     */
    @Schema(description = "직업 상세", example = "웹 디자이너")
    private String jobDetail;

    /**
     * 가입 방법
     * <p>
     * 사용자의 가입 방법입니다.
     * LOCAL: 이메일/비밀번호 회원가입
     * GOOGLE, KAKAO, NAVER: 소셜 로그인
     * </p>
     */
    @Schema(description = "가입 방법", example = "LOCAL")
    private Provider provider;

    /**
     * 이메일 인증 여부
     * <p>
     * 이메일 인증 완료 여부입니다.
     * 로컬 회원가입: 이메일 인증 후 true
     * 소셜 로그인: 자동으로 true
     * </p>
     */
    @Schema(description = "이메일 인증 여부", example = "true")
    private Boolean emailVerified;

    /**
     * 가입일시
     * <p>
     * 사용자가 회원가입한 일시입니다.
     * </p>
     */
    @Schema(description = "가입일시", example = "2025-01-01T00:00:00")
    private LocalDateTime createdAt;
}
