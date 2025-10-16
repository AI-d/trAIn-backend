package com.aid.train.backend.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원가입 응답 DTO입니다.
 * 회원가입 성공 시 생성된 사용자 정보를 반환합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "회원가입 응답")
public class SignupResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "면접왕")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/default.jpg")
    private String profileImageUrl;

    @Schema(description = "가입 일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "메시지", example = "회원가입이 완료되었습니다.")
    private String message;
}
