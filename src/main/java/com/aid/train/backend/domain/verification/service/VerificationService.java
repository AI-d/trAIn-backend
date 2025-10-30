package com.aid.train.backend.domain.verification.service;

import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.verification.dto.request.EmailResendRequestDto;
import com.aid.train.backend.domain.verification.dto.request.EmailVerificationRequestDto;
import com.aid.train.backend.domain.verification.dto.response.EmailVerificationResponseDto;
import com.aid.train.backend.domain.verification.entity.EmailVerification;
import com.aid.train.backend.domain.verification.repository.EmailVerificationRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 절차(Verification) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 이메일 인증 토큰/코드 발송 및 검증을 담당합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    /**
     * 사용자에게 인증 이메일을 발송합니다.
     *
     * @param user 인증을 진행할 User 엔티티
     * @return 생성된 emailVerificationToken (JWT)
     */
    @Transactional
    public String sendVerificationEmail(User user) {
        String verificationToken = jwtTokenProvider.generateEmailVerificationToken(user.getId(), user.getEmail());
        String otpCode = EmailVerification.generateOtpCode();

        EmailVerification emailVerification = EmailVerification.builder()
                .user(user)
                .email(user.getEmail())
                .verificationToken(verificationToken)
                .code(otpCode)
                .expiryDate(jwtTokenProvider.getExpiryDateTimeFromToken(verificationToken))
                .build();
        emailVerificationRepository.save(emailVerification);

        emailService.sendVerificationEmail(user.getEmail(), user.getName(), otpCode);

        log.info("이메일 인증 코드 발송 완료. Email: {}, Code: {}", user.getEmail(), otpCode);

        return verificationToken;
    }

    /**
     * 사용자가 입력한 인증 토큰과 코드를 검증합니다.
     *
     * @param request 이메일 인증 요청 DTO
     * @return 이메일 인증 응답 DTO
     */
    @Transactional
    public EmailVerificationResponseDto verifyEmail(EmailVerificationRequestDto request) {
        // 이메일 + 코드로 조회
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndCode(request.getEmail(), request.getVerificationCode())
                .orElseThrow(() -> {
                    log.warn("이메일 인증 실패 - 코드 불일치. Email: {}, Code: {}",
                            request.getEmail(), request.getVerificationCode());
                    return new TrainException(ErrorCode.VERIFICATION_CODE_INVALID);
                });

        // token도 검증 (이중 검증)
        if (!verification.getVerificationToken().equals(request.getEmailVerificationToken())) {
            log.warn("이메일 인증 실패 - 토큰 불일치. Email: {}", request.getEmail());
            throw new TrainException(ErrorCode.VERIFICATION_TOKEN_INVALID);
        }

        // 만료 검증
        if (verification.isExpired()) {
            log.warn("이메일 인증 실패 - 만료된 토큰. Email: {}", request.getEmail());
            throw new TrainException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        // 인증 완료 처리
        User user = verification.getUser();
        user.verifyEmail();
        emailVerificationRepository.delete(verification);

        log.info("이메일 인증 성공. Email: {}, Code: {}", request.getEmail(), request.getVerificationCode());

        return EmailVerificationResponseDto.builder()
                .success(true)
                .message("이메일 인증이 완료되었습니다.")
                .emailVerified(user.isEmailVerified())
                .build();
    }

    /**
     * 인증 이메일을 재발송하고 새 토큰을 반환합니다.
     * 기존 미인증 토큰을 삭제하고 새 토큰을 생성합니다.
     *
     * @param request 인증 코드 재발송 요청 DTO
     * @return 새로 생성된 emailVerificationToken (JWT)
     */
    @Transactional
    public String resendVerificationEmail(EmailResendRequestDto request) {
        User user = userRepository.findUnverifiedLocalUser(request.getEmail())
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND_OR_ALREADY_VERIFIED));

        // 1. 해당 이메일의 기존 미인증 토큰 모두 삭제
        int deletedCount = emailVerificationRepository.deleteUnverifiedByEmail(request.getEmail());

        // 2. 즉시 flush하여 DELETE 커밋
        emailVerificationRepository.flush();

        log.debug("이메일 재발송 - 기존 미인증 토큰 삭제: {} 개, Email: {}",
                deletedCount, request.getEmail());

        // TODO: 재발송 쿨타임(rate limiting) 로직 추가

        // 3. 새로운 인증 코드 생성 및 발송 (새 토큰 반환)
        String newToken = sendVerificationEmail(user);

        log.info("이메일 인증 코드 재발송 완료: Email: {}, 새 토큰 생성", request.getEmail());

        return newToken; // 새 토큰 반환
    }
}