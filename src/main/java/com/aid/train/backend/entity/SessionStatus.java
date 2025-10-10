package com.aid.train.backend.entity;

import lombok.Getter;

@Getter
public enum SessionStatus {
    ONGOING("진행중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;

    SessionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOngoing() {
        return this == ONGOING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isTerminated() {
        return this == COMPLETED || this == FAILED;
    }
}
