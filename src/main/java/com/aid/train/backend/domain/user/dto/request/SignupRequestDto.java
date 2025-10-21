package com.aid.train.backend.domain.user.dto.request;

import com.aid.train.backend.domain.user.enums.JobType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 로컬 회원가입 요청 DTO입니다.
 * 이메일, 비밀번호, 이름, 생년월일, 직업 정보를 받습니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "로컬 회원가입 요청")
public class SignupRequestDto {

    /**
     * 이메일 주소 (필수)
     */
    @Schema(description = "이메일", example = "user@example.com", required = true)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일은 최대 100자까지 입력 가능합니다.")
    private String email;

    /**
     * 비밀번호 (필수, 8~20자, 영문+숫자+특수문자)
     */
    @Schema(description = "비밀번호 (8~20자, 영문+숫자+특수문자)", example = "Password123!", required = true)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~20자의 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;

    /**
     * 사용자 이름 (필수)
     */
    @Schema(description = "이름", example = "홍길동", required = true)
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 최대 50자까지 입력 가능합니다.")
    private String name;

    /**
     * 생년월일 (필수)
     */
    @Schema(description = "생년월일", example = "1998-08-07", required = true)
    @NotNull(message = "생년월일은 필수입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * 직업 유형 (선택)
     */
    @Schema(description = "직업 유형", example = "EMPLOYEE", required = false)
    private JobType jobType;

    /**
     * 기타 직업 상세 (jobType이 OTHER인 경우만 필수)
     */
    @Schema(description = "기타 직업 상세 (jobType이 OTHER인 경우만)", example = "프리랜서 개발자", required = false)
    @Size(max = 100, message = "직업 상세는 최대 100자까지 입력 가능합니다.")
    private String jobDetail;

    /**
     * jobType이 OTHER인 경우 jobDetail 필수 검증
     *
     * @return 유효하면 true
     */
    public boolean isValid() {
        if (jobType == JobType.OTHER) {
            return jobDetail != null && !jobDetail.trim().isEmpty();
        }
        return true;
    }
}