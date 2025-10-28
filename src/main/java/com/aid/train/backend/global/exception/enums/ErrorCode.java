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

    // ===== 인증 및 권한 (401, 403) =====
    UNAUTHORIZED("AUTH_001", "인증이 필요합니다.", 401),
    FORBIDDEN("AUTH_002", "접근 권한이 없습니다.", 403),
    LOGIN_FAILED("AUTH_003", "이메일 또는 비밀번호가 올바르지 않습니다.", 401),
    USER_EMAIL_NOT_VERIFIED("AUTH_004", "이메일 인증이 완료되지 않았습니다.", 403),

    // ===== 토큰 관련 (401) =====
    TOKEN_INVALID("TOKEN_001", "유효하지 않은 토큰입니다.", 401),
    TOKEN_EXPIRED("TOKEN_002", "만료된 토큰입니다.", 401),
    TOKEN_INVALID_SIGNATURE("TOKEN_003", "토큰 서명이 유효하지 않습니다.", 401),
    TOKEN_UNSUPPORTED("TOKEN_004", "지원되지 않는 형식의 토큰입니다.", 401),
    REFRESH_TOKEN_INVALID("TOKEN_005", "리프레시 토큰이 유효하지 않습니다.", 401),

    // ===== 이메일 및 소셜 인증 토큰 (400, 401, 410) =====
    VERIFICATION_TOKEN_INVALID("VERIFY_001", "유효하지 않은 인증 세션입니다.", 401),
    VERIFICATION_TOKEN_EXPIRED("VERIFY_002", "인증 세션이 만료되었습니다. 다시 시도해주세요.", 410),
    VERIFICATION_CODE_INVALID("VERIFY_003", "인증 코드가 올바르지 않습니다.", 400),
    VERIFICATION_EMAIL_MISMATCH("VERIFY_004", "요청 이메일과 토큰의 이메일이 일치하지 않습니다.", 400),
    SOCIAL_SIGNUP_PENDING_TOKEN_INVALID("VERIFY_005", "소셜 회원가입 세션이 유효하지 않습니다.", 401),
    INVALID_ONE_TIME_CODE("VERIFY_006", "일회용 코드가 유효하지 않거나 만료되었습니다.", 401),
    EMAIL_SEND_FAILED("SERVER_002", "이메일 발송에 실패했습니다.", 500),

    // ===== 사용자 관련 (404, 409) =====
    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다.", 404),
    USER_EMAIL_DUPLICATED("USER_002", "이미 사용 중인 이메일입니다.", 409),
    USER_EMAIL_ALREADY_LINKED("USER_003", "이미 다른 방법으로 가입된 이메일입니다.", 409),
    USER_NOT_FOUND_OR_ALREADY_VERIFIED("USER_004", "사용자를 찾을 수 없거나 이미 인증된 사용자입니다.", 404),
    ALREADY_VERIFIED("USER_005", "이미 인증이 완료된 계정입니다.", 400),

    // ===== 비즈니스 규칙 위반 (400) =====
    PASSWORD_MISMATCH("RULE_001", "비밀번호가 일치하지 않습니다.", 400),
    USER_AGE_RESTRICTION("RULE_002", "만 14세 이상만 가입할 수 있습니다.", 400),
    JOB_DETAIL_REQUIRED("RULE_003", "기타 직업 선택 시 상세 정보는 필수입니다.", 400),
    CURRENT_PASSWORD_INVALID("RULE_004", "현재 비밀번호가 올바르지 않습니다.", 400),
    NEW_PASSWORD_SAME_AS_OLD("RULE_005", "새 비밀번호는 현재 비밀번호와 달라야 합니다.", 400),
    SOCIAL_USER_PASSWORD_CHANGE_NOT_ALLOWED("RULE_006", "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.", 400),

    // ===== 약관 관련 (400, 404) =====
    TERMS_NOT_FOUND_OR_VERSION_MISMATCH("TERMS_001", "약관을 찾을 수 없거나 버전이 일치하지 않습니다.", 404),
    REQUIRED_TERMS_NOT_AGREED("TERMS_002", "필수 약관에 동의해야 합니다.", 400),
    CANNOT_UPDATE_REQUIRED_TERMS("TERMS_003", "필수 약관의 동의 상태는 변경할 수 없습니다.", 400),

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
    AI_ANALYSIS_FAILED("AI_ANALYSIS_FAILED", "AI 분석에 실패했습니다.", 500),
    INSUFFICIENT_DIALOGUE_CONTENT("INSUFFICIENT_DIALOGUE_CONTENT", "분석할 대화 내용이 부족합니다.", 400),

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