package com.aid.train.backend.domain.user.dto.request;

import com.aid.train.backend.domain.user.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 프로필 수정 요청 DTO입니다.
 * <p>
 * 로그인한 사용자가 자신의 프로필 정보를 수정할 때 사용됩니다.
 * 이메일은 수정할 수 없으며, 이름, 생년월일, 직업 정보만 수정 가능합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Schema(description = "프로필 수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequestDto {

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
     * 생년월일 변경 시에도 미성년자 확인 로직이 적용됩니다.
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
