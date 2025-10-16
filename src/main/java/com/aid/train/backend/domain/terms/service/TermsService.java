package com.aid.train.backend.domain.terms.service;

import com.aid.train.backend.domain.terms.dto.response.TermsResponseDto;
import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.enums.TermsType;
import com.aid.train.backend.domain.terms.repository.TermsRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 약관 관리 서비스입니다.
 * 약관 조회 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TermsService {

    private final TermsRepository termsRepository;

    /**
     * 모든 활성화된 약관을 조회합니다.
     * TermsType의 displayOrder 순서대로 정렬하여 반환합니다.
     *
     * @return 약관 응답 DTO 목록
     */
    public List<TermsResponseDto> getAllActiveTerms() {
        log.info("[약관 조회] 전체 활성화 약관 조회 시작");

        List<Terms> termsList = termsRepository.findByIsActiveOrderByCreatedAtAsc(true);

        // TermsType의 displayOrder 순서대로 정렬
        List<TermsResponseDto> response = TermsType.sorted().stream()
                .map(type -> termsList.stream()
                        .filter(terms -> terms.getType() == type)
                        .findFirst()
                        .map(TermsResponseDto::from)
                        .orElse(null))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        log.info("[약관 조회] 전체 활성화 약관 조회 완료 - 개수: {}", response.size());
        return response;
    }

    /**
     * 특정 약관을 ID로 조회합니다.
     *
     * @param termsId 약관 ID
     * @return 약관 응답 DTO
     * @throws TrainException 약관을 찾을 수 없는 경우
     */
    public TermsResponseDto getTermsById(Long termsId) {
        log.info("[약관 조회] 약관 조회 시작 - ID: {}", termsId);

        Terms terms = termsRepository.findById(termsId)
                .orElseThrow(() -> {
                    log.warn("[약관 조회] 약관을 찾을 수 없음 - ID: {}", termsId);
                    return new TrainException(ErrorCode.TERMS_NOT_FOUND);
                });

        log.info("[약관 조회] 약관 조회 완료 - ID: {}, 타입: {}", termsId, terms.getType());
        return TermsResponseDto.from(terms);
    }

    /**
     * 특정 타입의 최신 활성화 약관을 조회합니다.
     *
     * @param type 약관 타입
     * @return 약관 응답 DTO
     * @throws TrainException 약관을 찾을 수 없는 경우
     */
    public TermsResponseDto getLatestTermsByType(TermsType type) {
        log.info("[약관 조회] 최신 약관 조회 시작 - 타입: {}", type);

        Terms terms = termsRepository.findByTypeAndIsActive(type, true)
                .orElseThrow(() -> {
                    log.warn("[약관 조회] 약관을 찾을 수 없음 - 타입: {}", type);
                    return new TrainException(ErrorCode.TERMS_NOT_FOUND);
                });

        log.info("[약관 조회] 최신 약관 조회 완료 - 타입: {}, 버전: {}", type, terms.getVersion());
        return TermsResponseDto.from(terms);
    }

    /**
     * 필수 약관 목록을 조회합니다.
     *
     * @return 필수 약관 응답 DTO 목록
     */
    public List<TermsResponseDto> getRequiredTerms() {
        log.info("[약관 조회] 필수 약관 조회 시작");

        List<TermsType> requiredTypes = Arrays.stream(TermsType.values())
                .filter(TermsType::isRequired)
                .collect(Collectors.toList());

        List<TermsResponseDto> response = requiredTypes.stream()
                .map(type -> termsRepository.findByTypeAndIsActive(type, true)
                        .map(TermsResponseDto::from)
                        .orElse(null))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        log.info("[약관 조회] 필수 약관 조회 완료 - 개수: {}", response.size());
        return response;
    }

    /**
     * 선택 약관 목록을 조회합니다.
     *
     * @return 선택 약관 응답 DTO 목록
     */
    public List<TermsResponseDto> getOptionalTerms() {
        log.info("[약관 조회] 선택 약관 조회 시작");

        List<TermsType> optionalTypes = Arrays.stream(TermsType.values())
                .filter(type -> !type.isRequired())
                .collect(Collectors.toList());

        List<TermsResponseDto> response = optionalTypes.stream()
                .map(type -> termsRepository.findByTypeAndIsActive(type, true)
                        .map(TermsResponseDto::from)
                        .orElse(null))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        log.info("[약관 조회] 선택 약관 조회 완료 - 개수: {}", response.size());
        return response;
    }
}
