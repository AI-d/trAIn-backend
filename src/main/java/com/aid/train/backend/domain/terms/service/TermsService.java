package com.aid.train.backend.domain.terms.service;

import com.aid.train.backend.domain.terms.dto.request.ConsentRequestDto;
import com.aid.train.backend.domain.terms.dto.request.ConsentUpdateRequestDto;
import com.aid.train.backend.domain.terms.dto.response.TermsResponseDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 약관 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 약관 조회, 사용자 약관 동의 생성 및 수정 기능을 제공합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserRepository userRepository;

    /**
     * 현재 활성화된 모든 약관 목록을 조회합니다.
     * <p>
     * 회원가입 시 사용자에게 보여줄 약관 목록을 가져오는 데 사용됩니다.
     * 약관은 {@code TermsType}의 {@code displayOrder}에 따라 정렬됩니다.
     * </p>
     *
     * @return 활성 약관 목록 DTO
     */
    public List<TermsResponseDto> getActiveTerms() {
        return termsRepository.findActiveTerms().stream()
                .map(TermsResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 약관 동의 내역을 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 약관 동의 목록 DTO
     */
    public List<UserConsentResponseDto> getUserConsents(Long userId) {
        return userConsentRepository.findByUserId(userId).stream()
                .map(UserConsentResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 제출한 약관 동의 목록의 유효성을 검증합니다.
     * <p>
     * DB에 저장된 모든 '필수' 약관에 대해 사용자가 '동의'했는지 확인합니다.
     * </p>
     *
     * @param consents 사용자가 제출한 약관 동의 목록 DTO
     * @throws TrainException 필수 약관에 동의하지 않았을 경우
     */
    public void validateConsents(List<ConsentRequestDto> consents) {
        // DB에서 현재 활성화된 필수 약관들의 ID 목록을 가져옴
        List<Long> requiredTermIds = termsRepository.findActiveRequiredTerms().stream()
                .map(Terms::getId)
                .toList();

        // 사용자가 동의한 필수 약관들의 ID 개수를 계산
        long requiredConsentedCount = consents.stream()
                .filter(ConsentRequestDto::getAgreed)
                .map(ConsentRequestDto::getTermsId)
                .filter(requiredTermIds::contains)
                .count();

        // 두 개수가 일치하지 않으면, 필수 약관에 모두 동의하지 않은 것임
        if (requiredConsentedCount != requiredTermIds.size()) {
            throw new TrainException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    /**
     * 회원가입 시 사용자의 약관 동의 내역을 DB에 저장합니다.
     *
     * @param user     새로 생성된 User 엔티티
     * @param consents 사용자가 제출한 약관 동의 목록 DTO
     * @throws TrainException 사용자가 동의한 약관이 존재하지 않거나 버전이 일치하지 않을 경우
     */
    @Transactional
    public void saveUserConsents(User user, List<ConsentRequestDto> consents) {
        // 성능 최적화를 위해 DB에서 모든 활성 약관을 한 번에 조회하여 Map으로 변환
        Map<Long, Terms> activeTermsMap = termsRepository.findActiveTerms().stream()
                .collect(Collectors.toMap(Terms::getId, Function.identity()));

        List<UserConsent> userConsents = consents.stream()
                .map(consentDto -> {
                    Terms terms = activeTermsMap.get(consentDto.getTermsId());
                    // 약관이 존재하지 않거나, 프론트엔드에서 보낸 버전과 DB의 버전이 다르면 예외 처리
                    if (terms == null || !terms.getVersion().equals(consentDto.getVersion())) {
                        throw new TrainException(ErrorCode.TERMS_NOT_FOUND_OR_VERSION_MISMATCH);
                    }
                    return UserConsent.builder()
                            .user(user)
                            .terms(terms)
                            .isAgreed(consentDto.getAgreed())
                            .consentedAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());

        userConsentRepository.saveAll(userConsents);
    }

    /**
     * 사용자의 약관 동의(주로 마케팅 등 선택 약관) 상태를 변경합니다.
     *
     * @param userId  현재 로그인한 사용자의 ID
     * @param request 약관 동의 변경 요청 DTO
     * @throws TrainException 필수 약관을 변경하려고 시도하거나, 약관 정보가 유효하지 않을 경우
     */
    @Transactional
    public void updateConsents(Long userId, ConsentUpdateRequestDto request) {
        // 필수 약관은 이 API를 통해 변경할 수 없음
        if (request.containsRequiredConsent()) {
            throw new TrainException(ErrorCode.CANNOT_UPDATE_REQUIRED_TERMS);
        }

        // User 엔티티를 직접 조회하지 않고, ID를 가진 프록시 객체를 사용하여 불필요한 SELECT 쿼리를 방지
        User userProxy = userRepository.getReferenceById(userId);

        request.getConsents().forEach(consentDto -> {
            Terms terms = termsRepository.findById(consentDto.getTermsId())
                    .orElseThrow(() -> new TrainException(ErrorCode.TERMS_NOT_FOUND_OR_VERSION_MISMATCH));

            // 해당 사용자의 특정 약관 타입에 대한 가장 최근 동의 내역을 찾음
            UserConsent userConsent = userConsentRepository.findLatestConsentByUserAndType(userId, terms.getType())
                    // 동의 내역이 없으면 새로 생성 (이때 프록시 User 객체 사용)
                    .orElseGet(() -> UserConsent.builder()
                            .user(userProxy)
                            .terms(terms)
                            .build());

            // 동의 상태를 업데이트하고 DB에 저장 (JPA의 변경 감지로 인해 save 호출은 선택적일 수 있으나 명시적으로 호출)
            userConsent.updateConsent(consentDto.getAgreed());
            userConsentRepository.save(userConsent);
        });
    }
}