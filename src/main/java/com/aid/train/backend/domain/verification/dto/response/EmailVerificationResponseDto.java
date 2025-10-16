package com.aid.train.backend.domain.verification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이메일 인증 응답 DTO입니다.
 * 이메일 인증 코드 발송 및 인증 결과를 반환합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "이메일 인증 응답")
public class EmailVerificationResponseDto {

    @Schema(description = "인증 성공 여부", example = "true")
    private Boolean isVerified;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "만료 시간 (분)", example = "10")
    private Integer expiresInMinutes;

    @Schema(description = "발송 시각")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @Schema(description = "메시지", example = "인증 코드가 발송되었습니다.")
    private String message;

    /**
     * 인증 코드 발송 응답을 생성합니다.
     *
     * @param email 이메일
     * @param expiresInMinutes 만료 시간 (분)
     * @return 발송 응답 DTO
     */
    public static EmailVerificationResponseDto ofSent(String email, Integer expiresInMinutes) {
        return EmailVerificationResponseDto.builder()
                .isVerified(false)
                .email(email)
                .expiresInMinutes(expiresInMinutes)
                .sentAt(LocalDateTime.now())
                .message("인증 코드가 발송되었습니다.")
                .build();
    }

    /**
     * 인증 성공 응답을 생성합니다.
     *
     * @param email 이메일
     * @return 인증 성공 응답 DTO
     */
    public static EmailVerificationResponseDto ofVerified(String email) {
        return EmailVerificationResponseDto.builder()
                .isVerified(true)
                .email(email)
                .message("이메일 인증이 완료되었습니다.")
                .build();
    }
}
