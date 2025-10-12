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
 * <p>
 * 휴면 계정 정책 (정책 B):
 * <ul>
 *   <li>휴면 계정도 로그인 가능</li>
 *   <li>로그인 시 자동으로 ACTIVE 상태로 전환</li>
 *   <li>별도 본인인증 불필요</li>
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
     * 1년 이상 미접속 시 자동 전환
     * 로그인 시 자동으로 ACTIVE로 복구
     */
    INACTIVE("휴면"),

    /**
     * 정지 상태
     * 약관 위반 등으로 관리자가 계정 정지
     * 관리자 승인 후 복구 가능
     */
    SUSPENDED("정지"),

    /**
     * 탈퇴 상태
     * 사용자가 직접 회원 탈퇴
     * 복구 불가
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
     * ACTIVE, INACTIVE 상태만 로그인 가능 (정책 B)
     *
     * <p>휴면 계정(INACTIVE)도 로그인을 허용하며,
     * 로그인 시 UserDetailsService에서 자동으로 ACTIVE로 전환합니다.</p>
     *
     * @return 로그인 가능하면 true, 아니면 false
     */
    public boolean canLogin() {
        return this == ACTIVE || this == INACTIVE;
    }

    /**
     * 복구 가능한 상태인지 확인합니다.
     * INACTIVE는 로그인 시 자동 복구
     * SUSPENDED는 관리자 승인 후 복구
     * WITHDRAWN은 복구 불가
     *
     * @return 복구 가능하면 true, 아니면 false
     */
    public boolean canRecover() {
        return this == INACTIVE || this == SUSPENDED;
    }

    /**
     * 완전히 종료된 상태인지 확인합니다.
     * 탈퇴한 계정은 접근 불가
     *
     * @return 종료 상태면 true, 아니면 false
     */
    public boolean isTerminated() {
        return this == WITHDRAWN;
    }
}