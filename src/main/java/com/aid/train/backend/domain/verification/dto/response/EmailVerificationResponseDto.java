package com.aid.train.backend.domain.verification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증 응답 DTO입니다.
 * <p>
 * 이메일 인증 성공 또는 실패 시 반환되는 정보를 담습니다.
 * 인증 성공 시 emailVerified=true로 변경되며, 이후 로그인이 가능합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "이메일 인증 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationResponseDto {

    /**
     * 인증 성공 여부
     * <p>
     * 이메일 인증이 성공하면 true, 실패하면 false입니다.
     * </p>
     */
    @Schema(description = "인증 성공 여부", example = "true")
    private Boolean success;

    /**
     * 안내 메시지
     * <p>
     * 사용자에게 표시할 안내 메시지입니다.
     * 성공: "이메일 인증이 완료되었습니다."
     * 실패: "유효하지 않은 인증 코드입니다.", "인증 토큰이 만료되었습니다." 등
     * </p>
     */
    @Schema(description = "안내 메시지", example = "이메일 인증이 완료되었습니다.")
    private String message;

    /**
     * 이메일 인증 여부
     * <p>
     * 사용자의 최종 이메일 인증 상태입니다.
     * 인증 성공 시 true로 변경됩니다.
     * </p>
     */
    @Schema(description = "이메일 인증 여부", example = "true")
    private Boolean emailVerified;
}
