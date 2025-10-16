package com.aid.train.backend.domain.verification.service;

import com.aid.train.backend.domain.user.dto.request.EmailVerificationRequestDto;
import com.aid.train.backend.domain.user.dto.request.VerifyCodeRequestDto;
import com.aid.train.backend.domain.user.dto.response.EmailVerificationResponseDto;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.verification.entity.EmailVerification;
import com.aid.train.backend.domain.verification.repository.EmailVerificationRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 이메일 인증 서비스입니다.
 * 이메일 인증 코드 발송 및 검증 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    // private final EmailSender emailSender; // TODO: 이메일 발송 구현 시 주입

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 10;

    /**
     * 이메일 인증 코드를 발송합니다.
     *
     * @param request 이메일 인증 요청 DTO
     * @return 이메일 인증 응답 DTO
     * @throws TrainException 이미 가입된 이메일인 경우
     */
    @Transactional
    public EmailVerificationResponseDto sendVerificationCode(EmailVerificationRequestDto request) {
        log.info("[이메일 인증] 인증 코드 발송 시작 - 이메일: {}", request.getEmail());

        // 1. 이메일 중복 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[이메일 인증] 이미 가입된 이메일 - {}", request.getEmail());
            throw new TrainException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. 기존 인증 정보 삭제 (재발송 대비)
        emailVerificationRepository.deleteByEmail(request.getEmail());

        // 3. 인증 코드 생성
        String code = generateVerificationCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);

        // 4. 인증 정보 저장
        EmailVerification verification = EmailVerification.createVerification(
                request.getEmail(),
                code,
                expiryDate
        );
        emailVerificationRepository.save(verification);

        // 5. 이메일 발송
        // TODO: 실제 이메일 발송 구현
        // emailSender.sendVerificationCode(request.getEmail(), code);
        log.info("[이메일 인증] 인증 코드 발송 완료 - 이메일: {}, 코드: {}", request.getEmail(), code);

        return EmailVerificationResponseDto.ofSent(request.getEmail(), expiryDate);
    }

    /**
     * 이메일 인증 코드를 검증합니다.
     *
     * @param request 인증 코드 검증 요청 DTO
     * @return 이메일 인증 응답 DTO
     * @throws TrainException 유효하지 않은 인증 코드인 경우
     * @throws TrainException 만료된 인증 코드인 경우
     */
    @Transactional
    public EmailVerificationResponseDto verifyCode(VerifyCodeRequestDto request) {
        log.info("[이메일 인증] 인증 코드 검증 시작 - 이메일: {}", request.getEmail());

        // 1. 인증 정보 조회
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndCode(request.getEmail(), request.getCode())
                .orElseThrow(() -> {
                    log.warn("[이메일 인증] 유효하지 않은 인증 코드 - 이메일: {}", request.getEmail());
                    return new TrainException(ErrorCode.INVALID_VERIFICATION_CODE);
                });

        // 2. 만료 검증
        if (verification.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("[이메일 인증] 만료된 인증 코드 - 이메일: {}", request.getEmail());
            throw new TrainException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        // 3. 이미 인증된 코드 검증
        if (verification.getIsVerified()) {
            log.warn("[이메일 인증] 이미 인증된 코드 - 이메일: {}", request.getEmail());
            throw new TrainException(ErrorCode.ALREADY_VERIFIED);
        }

        // 4. 인증 완료 처리
        verification.verify();

        log.info("[이메일 인증] 인증 코드 검증 완료 - 이메일: {}", request.getEmail());
        return EmailVerificationResponseDto.ofVerified(request.getEmail());
    }

    /**
     * 6자리 숫자 인증 코드를 생성합니다.
     * SecureRandom을 사용하여 예측 불가능한 코드를 생성합니다.
     *
     * @return 6자리 인증 코드
     */
    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }

        return code.toString();
    }
}
