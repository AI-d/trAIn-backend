package com.aid.train.backend.domain.terms.service;

import com.aid.train.backend.domain.terms.dto.request.UserConsentRequestDto;
import com.aid.train.backend.domain.terms.dto.response.UserConsentResponseDto;
import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.entity.UserConsent;
import com.aid.train.backend.domain.terms.repository.TermsRepository;
import com.aid.train.backend.domain.terms.repository.UserConsentRepository;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 약관 동의 서비스입니다.
 * 약관 동의 및 철회 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserConsentService {

    private final UserConsentRepository userConsentRepository;
    private final UserRepository userRepository;
    private final TermsRepository termsRepository;

    /**
     * 사용자의 모든 약관 동의 내역을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 약관 동의 응답 DTO 목록
     * @throws TrainException 사용자를 찾을 수 없는 경우
     */
    public List<UserConsentResponseDto> getUserConsents(Long userId) {
        log.info("[약관 동의 조회] 약관 동의 내역 조회 시작 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        List<UserConsent> consents = userConsentRepository.findByUser(user);

        log.info("[약관 동의 조회] 약관 동의 내역 조회 완료 - 사용자 ID: {}, 개수: {}", userId, consents.size());

        return consents.stream()
                .map(UserConsentResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 약관에 동의하거나 철회합니다.
     *
     * @param userId  사용자 ID
     * @param request 약관 동의 요청 DTO
     * @return 약관 동의 응답 DTO
     * @throws TrainException 사용자를 찾을 수 없는 경우
     * @throws TrainException 약관을 찾을 수 없는 경우
     * @throws TrainException 필수 약관을 철회하려는 경우
     */
    @Transactional
    public UserConsentResponseDto updateConsent(Long userId, UserConsentRequestDto request) {
        log.info("[약관 동의] 약관 동의/철회 시작 - 사용자 ID: {}, 약관 타입: {}, 동의 여부: {}",
                userId, request.getTermsType(), request.getIsAgreed());

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        // 2. 약관 조회
        Terms terms = termsRepository.findByTypeAndIsActive(request.getTermsType(), true)
                .orElseThrow(() -> {
                    log.warn("[약관 동의] 약관을 찾을 수 없음 - 타입: {}", request.getTermsType());
                    return new TrainException(ErrorCode.TERMS_NOT_FOUND);
                });

        // 3. 필수 약관 철회 방지
        if (!request.getIsAgreed() && terms.getType().isRequired()) {
            log.warn("[약관 동의] 필수 약관 철회 시도 - 사용자 ID: {}, 약관 타입: {}",
                    userId, request.getTermsType());
            throw new TrainException(ErrorCode.CANNOT_REVOKE_REQUIRED_TERMS);
        }

        // 4. 기존 동의 내역 조회 또는 생성
        UserConsent consent = userConsentRepository.findByUserAndTerms(user, terms)
                .orElseGet(() -> {
                    log.info("[약관 동의] 신규 약관 동의 생성 - 사용자 ID: {}, 약관 타입: {}",
                            userId, request.getTermsType());
                    return UserConsent.createConsent(user, terms, request.getIsAgreed());
                });

        // 5. 동의 상태 업데이트
        if (request.getIsAgreed()) {
            consent.agree();
            log.info("[약관 동의] 약관 동의 완료 - 사용자 ID: {}, 약관 타입: {}",
                    userId, request.getTermsType());
        } else {
            consent.revoke();
            log.info("[약관 동의] 약관 철회 완료 - 사용자 ID: {}, 약관 타입: {}",
                    userId, request.getTermsType());
        }

        // 6. 저장 (신규인 경우)
        if (consent.getId() == null) {
            userConsentRepository.save(consent);
        }

        return UserConsentResponseDto.from(consent);
    }

    /**
     * 특정 약관의 동의 상태를 확인합니다.
     *
     * @param userId    사용자 ID
     * @param termsType 약관 타입
     * @return 동의 여부
     * @throws TrainException 사용자를 찾을 수 없는 경우
     * @throws TrainException 약관을 찾을 수 없는 경우
     */
    public boolean isAgreed(Long userId, com.aid.train.backend.domain.terms.enums.TermsType termsType) {
        log.info("[약관 동의 확인] 약관 동의 확인 시작 - 사용자 ID: {}, 약관 타입: {}", userId, termsType);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        Terms terms = termsRepository.findByTypeAndIsActive(termsType, true)
                .orElseThrow(() -> new TrainException(ErrorCode.TERMS_NOT_FOUND));

        boolean isAgreed = userConsentRepository.existsByUserAndTermsAndIsAgreed(user, terms, true);

        log.info("[약관 동의 확인] 약관 동의 확인 완료 - 사용자 ID: {}, 약관 타입: {}, 동의 여부: {}",
                userId, termsType, isAgreed);

        return isAgreed;
    }
}
