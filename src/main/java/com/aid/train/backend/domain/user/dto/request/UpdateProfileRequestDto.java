package com.aid.train.backend.domain.user.dto.request;

import com.aid.train.backend.domain.user.enums.JobType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사용자 프로필 수정 요청 DTO입니다.
 * 이름, 생년월일, 직업 정보를 수정할 수 있습니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 프로필 수정 요청")
public class UpdateProfileRequestDto {

    /**
     * 사용자 이름 (필수)
     */
    @Schema(description = "사용자 이름", example = "홍길동", required = true)
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
     * 직업 유형 (필수)
     */
    @Schema(description = "직업 유형", example = "EMPLOYEE", required = true)
    @NotNull(message = "직업 유형은 필수입니다.")
    private JobType jobType;

    /**
     * 기타 직업 입력 (jobType이 OTHER인 경우에만 필수)
     * jobType 드롭다운에 없는 직업을 직접 입력
     */
    @Schema(description = "기타 직업 (jobType이 OTHER인 경우에만 필수)", example = "유튜버", required = false)
    @Size(max = 100, message = "기타 직업은 최대 100자까지 입력 가능합니다.")
    private String jobDetail;

    /**
     * jobType이 OTHER인 경우 jobDetail 필수 검증
     *
     * @return 유효하면 true
     */
    public boolean isValid() {
        // jobType이 OTHER인 경우에만 jobDetail 필수
        if (jobType == JobType.OTHER) {
            return jobDetail != null && !jobDetail.trim().isEmpty();
        }
        // jobType이 OTHER가 아니면 jobDetail은 무시 (null이어도 됨)
        return true;
    }
}