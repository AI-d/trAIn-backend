package com.aid.train.backend.domain.scenario.dto.response;

import com.aid.train.backend.domain.scenario.entity.Scenario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ScenarioResponseDto (

        String title,

        String description,

        String prompt,

        String voice,

        String difficulty,

        String category,

        Long ownerId,

        String locale
) {
    public static ScenarioResponseDto fromEntity(Scenario scenario) {
        return ScenarioResponseDto.builder()
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .prompt(scenario.getPrompt())
                .voice(scenario.getVoice().name())
                .difficulty(scenario.getDifficulty().name())
                .category(scenario.getCategory().name())
                .ownerId(scenario.getOwner() != null ? scenario.getOwner().getId() : null)
                .locale(scenario.getLocale())
                .build();
    }
}
