package com.aid.train.backend.global.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 에러 코드를 정의하는 Enum 클래스입니다.
 * 각 에러는 HTTP 상태 코드와 메시지를 가집니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // =========================
    // 인증 및 권한 관련
    // =========================
    NEED_LOGIN("NEED_LOGIN", "로그인이 필요한 작업입니다.", 401),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", 401),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", 403),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.", 409),
    INVALID_EMAIL("INVALID_EMAIL", "이메일이 올바르지 않습니다.", 401),
    INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호가 올바르지 않습니다.", 401),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.", 401),
    EMAIL_NOT_VERIFIED("EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다.", 403),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "이미 등록된 이메일입니다.", 409),
    INVALID_VERIFICATION_CODE("INVALID_VERIFICATION_CODE", "유효하지 않은 인증 코드입니다.", 400),
    VERIFICATION_CODE_EXPIRED("VERIFICATION_CODE_EXPIRED", "인증 코드가 만료되었습니다.", 410),
    ALREADY_VERIFIED("ALREADY_VERIFIED", "이미 인증이 완료되었습니다.", 400),

    // =========================
    // 토큰 관련
    // =========================
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다.", 401),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "만료된 토큰입니다.", 401),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다.", 401),
    INVALID_TEMP_TOKEN("INVALID_TEMP_TOKEN", "유효하지 않은 임시 토큰입니다.", 401),
    TEMP_TOKEN_EXPIRED("TEMP_TOKEN_EXPIRED", "임시 토큰이 만료되었습니다.", 401),

    // =========================
    // 사용자 관련
    // =========================
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", 404),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "이미 존재하는 사용자입니다.", 409),
    INACTIVE_USER("INACTIVE_USER", "비활성화된 사용자입니다.", 403),
    SUSPENDED_USER("SUSPENDED_USER", "정지된 사용자입니다.", 403),
    WITHDRAWN_USER("WITHDRAWN_USER", "탈퇴한 사용자입니다.", 410),
    USER_ALREADY_WITHDRAWN("USER_ALREADY_WITHDRAWN", "이미 탈퇴한 사용자입니다.", 409),
    USER_NOT_WITHDRAWN("USER_NOT_WITHDRAWN", "탈퇴하지 않은 사용자입니다.", 400),
    CANNOT_RESTORE_USER("CANNOT_RESTORE_USER", "복구 가능 기간이 지났습니다.", 400),
    INVALID_USER_STATUS("INVALID_USER_STATUS", "유효하지 않은 사용자 상태입니다.", 400),
    SOCIAL_USER_NO_PASSWORD("SOCIAL_USER_NO_PASSWORD", "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.", 400),
    SAME_AS_CURRENT_PASSWORD("SAME_AS_CURRENT_PASSWORD", "현재 비밀번호와 동일합니다.", 400),

    // =========================
    // 약관 관련
    // =========================
    TERMS_NOT_FOUND("TERMS_NOT_FOUND", "약관을 찾을 수 없습니다.", 404),
    REQUIRED_TERMS_NOT_AGREED("REQUIRED_TERMS_NOT_AGREED", "필수 약관에 동의해야 합니다.", 400),
    CANNOT_REVOKE_REQUIRED_TERMS("CANNOT_REVOKE_REQUIRED_TERMS", "필수 약관은 철회할 수 없습니다.", 400),

    // =========================
    // 공통 유효성 / 시스템 에러
    // =========================
    INVALID_INPUT("INVALID_INPUT", "입력값이 올바르지 않습니다.", 400),
    INVALID_INPUT_VALUE("INVALID_INPUT_VALUE", "잘못된 입력값입니다.", 400),
    INVALID_URL_FORMAT("INVALID_URL_FORMAT", "유효하지 않은 URL 형식입니다.", 400),
    ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", "엔티티를 찾을 수 없습니다.", 404),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", 404),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", 500),
    VALIDATION_ERROR("VALIDATION_ERROR", "유효성 검사에 실패했습니다.", 400),

    // =========================
    // 시나리오 관련
    // =========================
    SCENARIO_NOT_FOUND("SCENARIO_NOT_FOUND", "시나리오를 찾을 수 없습니다.", 404),
    SCENARIO_ALREADY_DELETED("SCENARIO_ALREADY_DELETED", "이미 삭제된 시나리오입니다.", 409),
    DEFAULT_SCENARIO_DELETE_FORBIDDEN("DEFAULT_SCENARIO_DELETE_FORBIDDEN", "기본 시나리오는 삭제할 수 없습니다.", 409),

    // =========================
    // 세션 관련
    // =========================
    SESSION_NOT_FOUND("SESSION_001", "세션을 찾을 수 없습니다.", 404),
    SESSION_ALREADY_COMPLETED("SESSION_002", "이미 종료된 세션입니다.", 400),
    SESSION_INVALID_STATUS("SESSION_003", "유효하지 않은 세션 상태입니다.", 400),

    // 비즈니스 에러 코드
    // =========================
    // 세션 관련
    // =========================
    // SESSION_NOT_FOUND("SESSION_NOT_FOUND", "세션을 찾을 수 없습니다.", 404), <- 충돌
    // SESSION_ALREADY_COMPLETED("SESSION_ALREADY_COMPLETED", "이미 완료된 세션입니다.", 409), < -충돌
    SESSION_NOT_OWNER("SESSION_NOT_OWNER", "세션 소유자가 아닙니다.", 403),

    // =========================
    // 피드백 관련
    // =========================
    FEEDBACK_NOT_FOUND("FEEDBACK_NOT_FOUND", "피드백을 찾을 수 없습니다.", 404),
    FEEDBACK_ALREADY_EXISTS("FEEDBACK_ALREADY_EXISTS", "이미 피드백이 생성되었습니다.", 409),

    // =========================
    // 히스토리 관련
    // =========================
    HISTORY_NOT_FOUND("HISTORY_NOT_FOUND", "히스토리를 찾을 수 없습니다.", 404),

    // =========================
    // AI 관련
    // =========================
    AI_API_ERROR("AI_API_ERROR", "AI API 호출 중 오류가 발생했습니다.", 500),
    AI_RESPONSE_PARSE_ERROR("AI_RESPONSE_PARSE_ERROR", "AI 응답 파싱 중 오류가 발생했습니다.", 500),

    // =========================
    // 비즈니스 로직 관련
    // =========================
    BUSINESS_ERROR("BUSINESS_ERROR", "비즈니스 로직 오류가 발생했습니다.", 400),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "이미 존재하는 리소스입니다.", 409),

    // =========================
    // 데이터베이스 관련
    // =========================
    DATA_INTEGRITY_VIOLATION("DATA_INTEGRITY_VIOLATION", "데이터 무결성 제약 조건을 위반했습니다.", 400);

    private final String code;
    private final String message;
    private final int status;

}