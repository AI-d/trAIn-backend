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
 * 로컬 회원가입 요청 DTO입니다.
 * <p>
 * 이메일/비밀번호 기반 회원가입 시 사용자가 입력하는 모든 필수 정보를 담습니다.
 * 회원가입 후 이메일 인증이 필요하며, 인증 완료 전까지는 로그인할 수 없습니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "로컬 회원가입 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDto {

    /**
     * 이메일 주소 (로그인 ID로 사용)
     * <p>
     * 실시간 중복 확인이 필요하며, 표준 이메일 형식을 따라야 합니다.
     * 예: example@domain.com
     * </p>
     */
    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;

    /**
     * 비밀번호
     * <p>
     * 8-20자, 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.
     * BCrypt로 해시화되어 저장됩니다.
     * </p>
     */
    @Schema(description = "비밀번호 (8-20자, 영문+숫자+특수문자)", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,20}$",
            message = "비밀번호는 8-20자의 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    private String password;

    /**
     * 비밀번호 확인
     * <p>
     * password 필드와 정확히 일치해야 합니다.
     * 프론트엔드와 백엔드 양쪽에서 검증합니다.
     * </p>
     */
    @Schema(description = "비밀번호 확인", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String passwordConfirm;

    /**
     * 사용자 이름 (닉네임)
     * <p>
     * 2-50자, 한글, 영문, 숫자, '.', '_', '-'만 허용됩니다.
     * 중복 확인은 하지 않으며, 동일한 이름을 가진 사용자가 여러 명 존재할 수 있습니다.
     * </p>
     */
    @Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2-50자여야 합니다")
    @Pattern(
            regexp = "^[가-힣a-zA-Z0-9._-]+$",
            message = "이름은 한글, 영문, 숫자, '.', '_', '-'만 사용 가능합니다"
    )
    private String name;

    /**
     * 생년월일
     * <p>
     * 과거 날짜만 허용되며, 만 14세 이상만 가입 가능합니다.
     * 미성년자 확인 로직은 서버에서 처리합니다.
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
     * jobType이 OTHER인 경우에만 입력하며, 20자 이하로 제한됩니다.
     * 예: "웹 디자이너", "유튜버", "작가"
     * </p>
     */
    @Schema(description = "직업 상세 (기타 선택 시 필수)", example = "웹 디자이너")
    @Size(max = 20, message = "직업 상세는 20자 이하여야 합니다")
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
     * 비밀번호와 비밀번호 확인이 일치하는지 검증합니다.
     *
     * @return 일치하면 true, 불일치하면 false
     */
    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }

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
