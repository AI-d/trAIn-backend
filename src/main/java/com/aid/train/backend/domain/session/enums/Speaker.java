package com.aid.train.backend.domain.session.enums;

import lombok.Getter;

@Getter
public enum Speaker {
    USER("사용자"),
    AI("AI");

    private final String description;

    Speaker(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUser() {
        return this == USER;
    }

    public boolean isAI() {
        return this == AI;
    }
}
