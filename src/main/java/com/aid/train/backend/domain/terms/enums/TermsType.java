package com.aid.train.backend.domain.terms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 약관 종류 Enum 클래스입니다.
 * 서비스에서 사용하는 약관의 종류를 구분합니다.
 *
 * <p>
 * 사용 위치:
 * <ul>
 *   <li>Terms.type: 약관의 종류</li>
 * </ul>
 * </p>
 *
 * <p>
 * 법적 요구사항:
 * <ul>
 *   <li>TERMS_OF_SERVICE: 서비스 이용약관 (필수 동의)</li>
 *   <li>PRIVACY_POLICY: 개인정보 처리방침 (필수 동의, 개인정보보호법 제39조의8)</li>
 *   <li>MARKETING_CONSENT: 마케팅 수신 동의 (선택 동의, 정보통신망법 제50조)</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum TermsType {

    /**
     * 서비스 이용약관 (필수)
     */
    TERMS_OF_SERVICE("이용약관", true),

    /**
     * 개인정보 처리방침 (필수)
     */
    PRIVACY_POLICY("개인정보 처리방침", true),

    /**
     * 마케팅 수신 동의 (선택)
     */
    MARKETING_CONSENT("마케팅 수신 동의", false);

    private final String displayName;
    private final boolean required;
}