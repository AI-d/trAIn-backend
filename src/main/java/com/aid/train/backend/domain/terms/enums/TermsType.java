package com.aid.train.backend.domain.terms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
 * <p>
 * 표시 순서:
 * <ul>
 *   <li>약관 팝업, 설정 화면 등에서 일관된 순서로 표시됩니다.</li>
 *   <li>sorted() 메서드로 정렬된 리스트를 제공합니다.</li>
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
     * 표시 순서: 1
     */
    TERMS_OF_SERVICE("이용약관", true, 1),

    /**
     * 개인정보 처리방침 (필수)
     * 표시 순서: 2
     */
    PRIVACY_POLICY("개인정보 처리방침", true, 2),

    /**
     * 마케팅 수신 동의 (선택)
     * 표시 순서: 3
     */
    MARKETING_CONSENT("마케팅 수신 동의", false, 3);

    private final String displayName;
    private final boolean required;
    private final int displayOrder;

    /**
     * 표시 순서대로 정렬된 약관 타입 리스트를 반환합니다.
     * 약관 팝업, 설정 화면 등에서 일관된 순서로 약관을 표시하기 위해 사용합니다.
     *
     * @return 표시 순서로 정렬된 약관 타입 리스트
     */
    public static List<TermsType> sorted() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(TermsType::getDisplayOrder))
                .toList();
    }
}