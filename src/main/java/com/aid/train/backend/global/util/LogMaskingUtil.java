package com.aid.train.backend.global.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * 로깅 시 개인정보 및 민감정보를 마스킹하는 유틸리티 클래스입니다.
 *
 * <p>
 * 이메일, ID, 토큰, Authorization 헤더 등
 * 로그에 직접 노출되어서는 안 되는 정보를 안전하게 처리합니다.
 * </p>
 *
 * <p>
 * 주요 특징:
 * <ul>
 *   <li>공통 마스킹 엔트리포인트 제공: {@link #maskSensitiveData(Object)}</li>
 *   <li>개별 항목별 세부 마스킹 메서드 제공</li>
 *   <li>민감 정보가 포함된 DTO, 로그 문자열 출력 시 재사용 가능</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
public final class LogMaskingUtil {

    private LogMaskingUtil() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    // ---------------------------------------------------
    // 공통 마스킹 엔트리포인트
    // ---------------------------------------------------

    /**
     * 객체 또는 문자열에 포함된 민감 정보를 마스킹합니다.
     *
     * <p>
     * 내부적으로 이메일, 토큰, Authorization 헤더 패턴 등을 탐지하여 자동 마스킹합니다.
     * null이거나 단순 타입이면 그대로 반환합니다.
     * </p>
     *
     * @param obj 로그로 출력될 객체 또는 문자열
     * @return 마스킹된 문자열 표현
     */
    public static String maskSensitiveData(Object obj) {
        if (obj == null) return "null";

        // 배열 또는 객체 배열 처리
        if (obj.getClass().isArray()) {
            return Arrays.stream((Object[]) obj)
                    .map(LogMaskingUtil::maskSensitiveData)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }

        String str = Objects.toString(obj);

        // --- 이메일 패턴 마스킹 ---
        if (str.contains("@")) {
            str = str.replaceAll("([\\w.%+-])([\\w.%+-]*)@([\\w.-]+)", "$1***@$3");
        }

        // --- Authorization 헤더 ---
        if (str.toLowerCase().contains("bearer ")) {
            str = str.replaceAll("(?i)Bearer [A-Za-z0-9\\-._~+/]+=*", "Bearer ****");
        }

        // --- Token, Refresh, Access 등 키워드 포함 문자열 ---
        if (str.toLowerCase().contains("token")) {
            str = str.replaceAll("(?i)(token=)[A-Za-z0-9\\-._~+/]+", "$1****");
        }

        // --- Password ---
        if (str.toLowerCase().contains("password")) {
            str = str.replaceAll("(?i)(password=)[^,&\\s]+", "$1****");
        }

        return str;
    }

    // ---------------------------------------------------
    // 이메일
    // ---------------------------------------------------
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        String maskedLocalPart = localPart.substring(0, 1) + "***";
        return maskedLocalPart + domain;
    }

    // ---------------------------------------------------
    // 사용자 ID
    // ---------------------------------------------------
    public static String maskUserId(Long userId) {
        if (userId == null) {
            return "***";
        }

        String idStr = userId.toString();
        if (idStr.length() <= 2) {
            return "***";
        }

        return "***" + idStr.substring(idStr.length() - 2);
    }

    // ---------------------------------------------------
    // 토큰
    // ---------------------------------------------------
    public static String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "***";
        }

        int length = token.length();
        if (length <= 8) {
            return "****";
        }

        // 앞 4자리 + **** + 뒤 4자리 남기기
        return token.substring(0, 4) + "****" + token.substring(length - 4);
    }

    // ---------------------------------------------------
    // Authorization 헤더
    // ---------------------------------------------------
    public static String maskAuthorizationHeader(String header) {
        if (header == null || header.isBlank()) {
            return "Bearer ****";
        }
        if (header.toLowerCase().startsWith("bearer ")) {
            return "Bearer ****";
        }
        return "****";
    }
}
