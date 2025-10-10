package com.aid.train.backend.domain.scenario.dto.request;

import com.aid.train.backend.domain.scenario.entity.Scenario.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ScenarioRequestDto (

        @NotBlank(message = "제목은 필수 입니다.")
        @Size(max = 120)
        String title,

        @NotBlank(message = "설명은 필수 입니다.")
        String description,

        @NotBlank(message = "프롬프트는 필수 입니다.")
        String prompt,

        @NotNull
        Voice voice,

        @NotNull
        Difficulty difficulty,

        @NotNull
        Category category,

        Long ownerId,

        @NotBlank(message = "지역 선택은 필수입니다.")
        @Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$", message = "지역은 예: ko-KR, en-US 형식이어야 합니다.")
        String locale
) {
}
