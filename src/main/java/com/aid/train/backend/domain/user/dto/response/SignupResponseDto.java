package com.aid.train.backend.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 응답 DTO입니다.
 * <p>
 * 로컬 회원가입 성공 시 반환되는 정보를 담습니다.
 * 회원가입 후 이메일 인증이 필요하며, 인증 완료 전까지는 로그인할 수 없습니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "회원가입 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponseDto {

    /**
     * 생성된 사용자 ID
     * <p>
     * 데이터베이스에 저장된 사용자의 고유 식별자입니다.
     * </p>
     */
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    /**
     * 이메일 주소
     * <p>
     * 회원가입 시 입력한 이메일 주소입니다.
     * 이 이메일로 인증 코드가 발송됩니다.
     * </p>
     */
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    /**
     * 사용자 이름
     * <p>
     * 회원가입 시 입력한 이름입니다.
     * </p>
     */
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    /**
     * 이메일 인증 여부
     * <p>
     * 회원가입 직후에는 항상 false입니다.
     * 이메일 인증을 완료해야 true로 변경됩니다.
     * </p>
     */
    @Schema(description = "이메일 인증 여부", example = "false")
    private Boolean emailVerified;

    /**
     * 안내 메시지
     * <p>
     * 사용자에게 표시할 안내 메시지입니다.
     * 예: "회원가입이 완료되었습니다. 이메일 인증을 진행해주세요."
     * </p>
     */
    @Schema(description = "안내 메시지", example = "회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.")
    private String message;

    /**
     * 이메일 인증에 사용할 토큰 (15분 유효)
     * 프론트엔드는 이 토큰을 저장해두었다가,
     * 사용자가 이메일 코드를 입력하면 함께 전송해야 합니다.
     */
    @Schema(description = "이메일 인증 토큰 (15분 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String emailVerificationToken;
}
