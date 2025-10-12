package com.aid.train.backend.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 계정 상태 Enum 클래스입니다.
 * 사용자 계정의 현재 상태를 구분합니다.
 *
 * <p>
 * 사용 위치:
 * <ul>
 *   <li>User.status: 사용자 계정의 현재 상태</li>
 * </ul>
 * </p>
 *
 * <p>
 * 상태 전환 규칙:
 * <ul>
 *   <li>회원가입 → ACTIVE</li>
 *   <li>1년 미접속 → INACTIVE (휴면 전환)</li>
 *   <li>약관 위반 → SUSPENDED (관리자 정지)</li>
 *   <li>회원 탈퇴 → WITHDRAWN (복구 불가)</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {

    /**
     * 활성 상태 (정상)
     */
    ACTIVE("활성"),

    /**
     * 비활성 상태 (휴면)
     */
    INACTIVE("휴면"),

    /**
     * 정지 상태
     */
    SUSPENDED("정지"),

    /**
     * 탈퇴 상태
     */
    WITHDRAWN("탈퇴");

    private final String displayName;

    /**
     * 활성 상태인지 확인합니다.
     *
     * @return 활성 상태면 true, 아니면 false
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 로그인 가능한 상태인지 확인합니다.
     * ACTIVE 상태만 로그인 가능
     *
     * @return 로그인 가능하면 true, 아니면 false
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }
}