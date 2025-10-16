package com.aid.train.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로필 수정 요청 DTO입니다.
 * 닉네임, 프로필 이미지 등 프로필 정보 수정 시 사용됩니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "프로필 수정 요청 DTO")
public class UpdateProfileRequestDto {

    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    @Schema(description = "닉네임 (2~20자, 선택)", example = "새로운닉네임")
    private String nickname;

    @Pattern(regexp = "^https?://.+", message = "올바른 URL 형식이 아닙니다.")
    @Size(max = 2048, message = "URL은 2048자를 초과할 수 없습니다.")
    @Schema(description = "프로필 이미지 URL (선택)", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

    /**
     * 테스트용 생성자입니다.
     *
     * @param nickname         닉네임
     * @param profileImageUrl  프로필 이미지 URL
     */
    public UpdateProfileRequestDto(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
