package com.aid.train.backend.domain.user.dto.request;

import com.aid.train.backend.domain.terms.dto.request.ConsentRequestDto;
import com.aid.train.backend.domain.user.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 소셜 회원가입 완료 요청 DTO입니다.
 * <p>
 * 소셜 로그인(Google, Kakao, Naver) 후 신규 사용자가 추가 정보를 입력하여
 * 회원가입을 완료할 때 사용됩니다.
 * SOCIAL_SIGNUP_PENDING_TOKEN(15분 유효)을 검증한 후 User와 SocialAccount를 생성합니다.
 * 소셜 계정은 이미 제공자에서 이메일 인증이 완료되었으므로 emailVerified=true가 자동 설정됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "소셜 회원가입 완료 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialSignupCompleteRequestDto {

    /**
     * 소셜 회원가입 대기 토큰
     * <p>
     * 소셜 로그인 콜백 시 발급받은 SOCIAL_SIGNUP_PENDING_TOKEN입니다.
     * 15분간 유효하며, 이 토큰에는 소셜 제공자 정보, 이메일, 이름이 포함되어 있습니다.
     * 토큰이 만료되면 처음부터 다시 소셜 로그인을 진행해야 합니다.
     * </p>
     */
    @Schema(description = "소셜 회원가입 대기 토큰 (15분 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "소셜 회원가입 대기 토큰은 필수입니다")
    private String socialSignupPendingToken;

    /**
     * 생년월일
     * <p>
     * 과거 날짜만 허용되며, 만 14세 이상만 가입 가능합니다.
     * 소셜 로그인에서는 생년월일을 제공하지 않으므로 사용자가 직접 입력해야 합니다.
     * </p>
     */
    @Schema(description = "생년월일", example = "1990-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate;

    /**
     * 직업 유형
     * <p>
     * 드롭다운에서 선택합니다: 직장인, 학생, 자영업자, 프리랜서, 기타
     * 기타 선택 시 jobDetail 필드가 필수가 됩니다.
     * </p>
     */
    @Schema(description = "직업 유형", example = "EMPLOYEE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "직업은 필수입니다")
    private JobType jobType;

    /**
     * 직업 상세 (기타 선택 시 필수)
     * <p>
     * jobType이 OTHER인 경우에만 입력하며, 100자 이하로 제한됩니다.
     * 소셜 회원가입에서는 로컬 회원가입보다 더 긴 입력을 허용합니다.
     * 예: "프리랜서 웹 디자이너", "유튜버 겸 작가"
     * </p>
     */
    @Schema(description = "직업 상세 (기타 선택 시 필수)", example = "프리랜서 웹 디자이너")
    @Size(max = 100, message = "직업 상세는 100자 이하여야 합니다")
    private String jobDetail;

    /**
     * 약관 동의 목록
     * <p>
     * 필수 약관 2개(이용약관, 개인정보처리방침)는 반드시 동의해야 하며,
     * 선택 약관(마케팅 수신 등)은 선택적으로 동의할 수 있습니다.
     * </p>
     */
    @Schema(description = "약관 동의 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "약관 동의는 필수입니다")
    @Valid
    private List<ConsentRequestDto> consents;

    /**
     * 만 14세 미만인지 확인합니다.
     *
     * @return 만 14세 미만이면 true, 이상이면 false
     */
    public boolean isMinor() {
        if (birthDate == null) {
            return true;
        }
        return birthDate.isAfter(LocalDate.now().minusYears(14));
    }

    /**
     * 직업이 기타인 경우 직업 상세가 필수인지 검증합니다.
     *
     * @return 기타인데 상세가 없으면 false, 그 외는 true
     */
    public boolean isJobDetailValid() {
        if (jobType == JobType.OTHER) {
            return jobDetail != null && !jobDetail.isBlank();
        }
        return true;
    }
}
