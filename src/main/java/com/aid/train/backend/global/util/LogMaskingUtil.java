package com.aid.train.backend.global.util;

/**
 * 로깅 시 개인정보를 마스킹하는 유틸리티 클래스입니다.
 * 개인정보보호법 준수를 위해 이메일, ID 등을 로그에 남길 때 사용합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
public final class LogMaskingUtil {

    private LogMaskingUtil() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /**
     * 이메일을 마스킹합니다.
     * 예: user@example.com -> u***@example.com
     *
     * @param email 원본 이메일
     * @return 마스킹된 이메일
     */
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

        // 첫 글자만 남기고 나머지는 ***
        String maskedLocalPart = localPart.substring(0, 1) + "***";

        return maskedLocalPart + domain;
    }

    /**
     * 사용자 ID를 마스킹합니다.
     * 예: 12345 -> ***45
     *
     * @param userId 사용자 ID
     * @return 마스킹된 ID
     */
    public static String maskUserId(Long userId) {
        if (userId == null) {
            return "***";
        }

        String idStr = userId.toString();
        if (idStr.length() <= 2) {
            return "***";
        }

        // 뒤 2자리만 남기고 나머지는 ***
        return "***" + idStr.substring(idStr.length() - 2);
    }
}
