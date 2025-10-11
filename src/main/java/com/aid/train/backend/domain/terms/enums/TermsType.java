package com.aid.train.backend.domain.terms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum TermsType {
    TERMS_OF_SERVICE("이용약관", true),
    PRIVACY_POLICY("개인정보 처리방침", true),
    MARKETING_CONSENT("마케팅 수신 동의", false);

    private final String displayName;
    private final boolean required;
}
