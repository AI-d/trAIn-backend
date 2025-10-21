package com.aid.train.backend.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 직업 유형 Enum 클래스입니다.
 * 사용자의 직업 정보를 관리하며, 향후 직업군별 맞춤 서비스 제공에 활용됩니다.
 *
 * <p>
 * 사용 위치:
 * <ul>
 *   <li>User.jobType: 사용자의 직업 유형</li>
 *   <li>회원가입 시 직업 선택</li>
 *   <li>사용자 프로필 관리</li>
 * </ul>
 * </p>
 *
 * <p>
 * 비즈니스 규칙:
 * <ul>
 *   <li>OTHER 선택 시 jobDetail 필드에 상세 입력 필요</li>
 *   <li>나머지 항목들은 jobDetail 불필요</li>
 * </ul>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum JobType {

    /**
     * 직장인 (회사원, 공무원 등)
     */
    EMPLOYEE("직장인"),

    /**
     * 학생 (대학생, 대학원생, 고등학생 등)
     */
    STUDENT("학생"),

    /**
     * 주부/주부
     */
    HOUSEWIFE("주부"),

    /**
     * 프리랜서 (개인사업자, 자영업자 포함)
     */
    FREELANCER("프리랜서"),

    /**
     * 사업가/기업가
     */
    BUSINESS_OWNER("사업가"),

    /**
     * 구직자/취업준비생
     */
    JOB_SEEKER("구직자"),

    /**
     * 은퇴자
     */
    RETIRED("은퇴자"),

    /**
     * 기타 (상세 입력 필요)
     */
    OTHER("기타");

    private final String displayName;

    /**
     * '기타' 직업 선택 시 상세 입력이 필요한지 확인합니다.
     *
     * @return OTHER 타입이면 true, 아니면 false
     */
    public boolean requiresDetail() {
        return this == OTHER;
    }

    /**
     * 표시명으로 JobType을 찾습니다.
     * 대소문자를 무시하고 검색합니다.
     *
     * @param displayName 표시명
     * @return 해당하는 JobType
     * @throws IllegalArgumentException 일치하는 JobType이 없는 경우
     */
    public static JobType fromDisplayName(String displayName) {
        if (displayName == null) {
            throw new IllegalArgumentException("displayName은 null일 수 없습니다.");
        }

        for (JobType jobType : values()) {
            if (jobType.displayName.equalsIgnoreCase(displayName.trim())) {
                return jobType;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 직업 유형입니다: " + displayName);
    }
}