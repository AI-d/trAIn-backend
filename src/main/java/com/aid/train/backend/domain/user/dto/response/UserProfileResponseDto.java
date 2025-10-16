package com.aid.train.backend.domain.user.dto.response;

import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 프로필 응답 DTO입니다.
 * 사용자의 기본 정보와 연동된 소셜 계정 정보를 반환합니다.
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
    private Long userId;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "면접왕")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/123.jpg")
    private String profileImageUrl;

    @Schema(description = "계정 상태", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "연동된 소셜 계정 목록", example = "[\"GOOGLE\", \"KAKAO\"]")
    private List<Provider> connectedProviders;

    @Schema(description = "마지막 로그인 시각")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @Schema(description = "가입 일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * User 엔티티를 DTO로 변환합니다.
     *
     * @param user User 엔티티
     * @param connectedProviders 연동된 소셜 계정 목록
     * @return UserProfileResponseDto
     */
    public static UserProfileResponseDto from(User user, List<Provider> connectedProviders) {
        return UserProfileResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .status(user.getStatus())
                .connectedProviders(connectedProviders)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
