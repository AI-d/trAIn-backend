package com.aid.train.backend.domain.terms.initializer;

import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.enums.TermsType;
import com.aid.train.backend.domain.terms.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 약관 데이터 초기화 컴포넌트입니다.
 * 애플리케이션 시작 시 필요한 약관 데이터를 자동으로 생성합니다.
 *
 * <p>
 * 이미 존재하는 약관은 중복 생성하지 않으며, 누락된 약관만 추가합니다.
 * TermsType의 required 속성에 따라 제목에 [필수]/[선택] 접두사를 자동으로 추가합니다.
 * </p>
 *
 * <p>
 * 생성되는 약관:
 * <ul>
 *   <li>서비스 이용약관 (필수)</li>
 *   <li>개인정보 처리방침 (필수)</li>
 *   <li>마케팅 수신 동의 (선택)</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TermsDataInitializer {

    private final TermsRepository termsRepository;

    /**
     * 애플리케이션 시작 완료 후 약관 데이터를 초기화합니다.
     *
     * <p>
     * ApplicationReadyEvent를 사용하여 모든 빈이 완전히 초기화된 후 실행됩니다.
     * 기존 약관이 있는지 확인하고, 없는 경우에만 새로 생성합니다.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeTermsData() {
        log.info("약관 데이터 초기화 시작");

        // 약관 생성 (TermsType.sorted() 순서대로)
        for (TermsType type : TermsType.sorted()) {
            createTermsIfNotExists(type);
        }

        log.info("약관 데이터 초기화 완료");
    }

    /**
     * 약관이 존재하지 않는 경우에만 생성합니다.
     * TermsType의 required 속성에 따라 제목에 [필수]/[선택] 접두사를 자동 추가합니다.
     *
     * @param type 약관 타입
     */
    private void createTermsIfNotExists(TermsType type) {
        boolean exists = termsRepository.findByTypeAndIsActive(type, true).isPresent();

        if (!exists) {
            String title = addRequiredPrefix(type);
            String content = getTermsContent(type);
            Terms terms = Terms.createTerms(type, title, content, "1.0");
            termsRepository.save(terms);
            log.info("약관 생성 완료 - 타입: {}, 제목: {}", type, title);
        } else {
            log.debug("약관 이미 존재 - 타입: {}", type);
        }
    }

    /**
     * TermsType의 required 속성에 따라 제목에 접두사를 추가합니다.
     *
     * @param type 약관 타입
     * @return 접두사가 추가된 제목
     */
    private String addRequiredPrefix(TermsType type) {
        String prefix = type.isRequired() ? "[필수] " : "[선택] ";
        return prefix + type.getDisplayName();
    }

    /**
     * 약관 타입에 따라 약관 내용을 반환합니다.
     *
     * @param type 약관 타입
     * @return 약관 내용
     */
    private String getTermsContent(TermsType type) {
        return switch (type) {
            case TERMS_OF_SERVICE -> getTermsOfServiceContent();
            case PRIVACY_POLICY -> getPrivacyPolicyContent();
            case MARKETING_CONSENT -> getMarketingConsentContent();
        };
    }

    // ===== 약관 내용 정의 메서드들 =====

    /**
     * 서비스 이용약관 내용을 반환합니다.
     *
     * @return 서비스 이용약관 내용
     */
    private String getTermsOfServiceContent() {
        return """
                # 서비스 이용약관

                ## 제1조 (목적)
                본 약관은 AI-d(이하 "회사")이 제공하는 AI 대화 연습 서비스(이하 "서비스")의 이용조건 및 절차를 정함을 목적으로 합니다.

                ## 제2조 (정의)
                1) "서비스"란 회사가 제공하는 AI 대화 연습 및 그 부가 기능 일체를 말합니다.
                2) "회원"이란 본 약관에 동의하고 회사와 이용계약을 체결한 자를 말합니다.
                3) "시나리오"란 회사가 제공하는 대화 연습 상황을 말합니다.

                ## 제3조 (약관의 효력 및 변경)
                1) 본 약관은 서비스 화면에 게시하거나 기타 방법으로 공지함으로써 효력이 발생합니다.
                2) 회사는 관련 법령을 위반하지 않는 범위에서 약관을 변경할 수 있으며, 변경 시 사전 공지합니다.

                ## 제4조 (서비스 제공 및 변경)
                1) 회사는 연중무휴, 1일 24시간 서비스를 제공합니다. 단, 정기점검 등 불가피한 경우 서비스가 일시 중단될 수 있습니다.
                2) 서비스의 내용, 운영상 또는 기술상 필요에 따라 변경·중단될 수 있으며, 이 경우 사전에 공지합니다.

                ## 제5조 (회원의 의무)
                1) 회원은 관계 법령, 약관, 운영정책 등을 준수하여야 합니다.
                2) 타인의 개인정보 침해, 서비스의 안정적 운영을 방해하는 행위 등은 금지됩니다.
                3) AI 대화 연습 과정에서 생성되는 음성 및 텍스트 데이터는 서비스 개선을 위해 활용될 수 있습니다.

                ## 제6조 (계정 관리)
                계정 및 비밀번호 관리 책임은 회원에게 있으며, 분실·도용에 대한 주의의무를 다해야 합니다.

                ## 제7조 (손해배상 및 면책)
                1) 회사는 회사의 고의 또는 중대한 과실이 없는 한 서비스 이용과 관련하여 발생한 손해에 대하여 책임을 지지 않습니다.
                2) 불가항력(천재지변, 서버 장애 등)에 의한 서비스 중단에 대해서도 면책됩니다.
                3) AI 대화 피드백은 참고용이며, 실제 대화 실력 향상의 결과는 사용자 개인의 달려있습니다..

                ## 제8조 (준거법 및 관할법원)
                본 약관과 서비스 이용에 관한 분쟁은 대한민국 법을 준거법으로 하며, 관할법원은 민사소송법에 따릅니다.

                ## 부칙
                본 약관은 2025년 10월 12일부터 시행됩니다.
                """;
    }

    /**
     * 개인정보 처리방침 내용을 반환합니다.
     *
     * @return 개인정보 처리방침 내용
     */
    private String getPrivacyPolicyContent() {
        return """
                # 개인정보 처리방침

                본 방침은 개인정보 보호법, 정보통신망 이용촉진 및 정보보호 등에 관한 법률 등 관계 법령을 준수합니다.

                ## 1. 개인정보의 처리 목적
                - 회원가입 및 본인확인
                - AI 대화 연습 서비스 제공 및 개선
                - 대화 피드백 생성 및 제공
                - 맞춤형 추천 제공
                - 고객 상담 및 불만 처리

                ## 2. 개인정보의 처리 및 보유 기간
                - 회원정보(이메일, 닉네임 등): 회원 탈퇴 시까지
                - 대화 히스토리 및 피드백: 회원 탈퇴 시까지
                - 서비스 이용기록/접속기록: 3년 보관 (관련 법령에 따라)
                - 탈퇴 회원 정보: 탈퇴 후 30일 보관 (복구 요청 대응)

                ## 3. 개인정보의 수집 항목
                ### 필수 항목
                - 이메일, 닉네임, 비밀번호(로컬 가입 시), 직업, 나이
                - 대화 음성 데이터, 대화 텍스트 데이터
                - 서비스 이용 기록

                ### 선택 항목
                - 프로필 이미지
                - 개인화 설정 값

                ## 4. 개인정보의 제3자 제공
                원칙적으로 제3자에게 제공하지 않습니다. 다만 다음의 경우 예외로 합니다.
                - 정보주체의 동의가 있는 경우
                - 법령에 근거한 요청이 있는 경우

                ## 5. 개인정보 처리의 위탁
                서비스 운영을 위해 일부 업무를 위탁할 수 있습니다.
                - 수탁업체: AWS (데이터 보관/호스팅)
                - 위탁업무: 인프라 운영 및 데이터 보관
                - 수탁업체: OpenAI (AI 음성 처리)
                - 위탁업무: 음성 인식 및 AI 응답 생성

                위탁계약 체결 시 개인정보 보호 관련 의무를 규정합니다.

                ## 6. 정보주체의 권리
                이용자는 언제든지 개인정보 열람·정정·삭제·처리정지 등을 요구할 수 있습니다.

                ## 7. 개인정보의 파기
                보유기간 경과 또는 처리 목적 달성 시 지체 없이 파기합니다.

                ## 8. 개인정보 보호책임자
                - 팀: Aid
                - 이메일: dialogym.official@gmail.com

                ## 9. 개인정보의 안전성 확보 조치
                - 개인정보의 암호화
                - 해킹 등에 대비한 기술적 대책
                - 개인정보 취급 직원의 최소화 및 교육
                - 개인정보보호 전담기구의 운영

                ## 10. 고지의 의무
                본 방침이 변경되는 경우 서비스 공지사항 등을 통해 공지합니다.

                ## 부칙
                본 방침은 2025년 10월 12일부터 시행됩니다.
                """;
    }

    /**
     * 마케팅 수신 동의 내용을 반환합니다.
     *
     * @return 마케팅 수신 동의 내용
     */
    private String getMarketingConsentContent() {
        return """
                # 마케팅 수신 동의

                ## 1. 목적
                신규 서비스 안내, 이벤트 정보 제공, 맞춤형 광고 제공

                ## 2. 수집 항목
                - 이메일 주소
                - 서비스 이용 패턴
                - 선호도 정보

                ## 3. 보유 및 이용 기간
                - 동의 철회 시 또는 회원 탈퇴 시까지

                ## 4. 수신 방법
                - 이메일
                - 서비스 내 알림

                ## 5. 동의 거부권 및 불이익
                본 동의는 선택사항입니다. 동의하지 않아도 서비스 이용에 제한이 없습니다.
                단, 마케팅 정보 수신은 불가능합니다.

                ## 6. 동의 철회
                마이페이지 > 설정 > 알림 설정에서 언제든지 철회 가능합니다.

                ## 부칙
                본 동의서는 2025년 10월 12일부터 시행됩니다.
                """;
    }
}
