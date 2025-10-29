package com.aid.train.backend.domain.verification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증 코드 재발송 응답 DTO
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendResponseDto {

    /**
     * 재발송 성공 여부
     */
    private Boolean success;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 새로 발급된 이메일 인증 토큰 (JWT)
     */
    private String emailVerificationToken;
}