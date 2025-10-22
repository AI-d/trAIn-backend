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
@Transactional
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

        return verificationToken; // 생성된 토큰을 반환
    }

    /**
     * 사용자가 입력한 인증 토큰과 코드를 검증합니다.
     *
     * @param request 이메일 인증 요청 DTO
     * @return 이메일 인증 응답 DTO
     */
    public EmailVerificationResponseDto verifyEmail(EmailVerificationRequestDto request) {
        EmailVerification verification = emailVerificationRepository
                .findByVerificationToken(request.getEmailVerificationToken())
                .orElseThrow(() -> new TrainException(ErrorCode.VERIFICATION_TOKEN_INVALID));

        if (verification.isExpired()) {
            throw new TrainException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }
        if (!verification.getCode().equals(request.getVerificationCode())) {
            // TODO: 실패 횟수 카운트 및 계정 잠금 로직 추가
            throw new TrainException(ErrorCode.VERIFICATION_CODE_INVALID);
        }
        if (!verification.getEmail().equals(request.getEmail())) {
            throw new TrainException(ErrorCode.VERIFICATION_EMAIL_MISMATCH);
        }

        User user = verification.getUser();
        user.verifyEmail();
        emailVerificationRepository.delete(verification); // 인증 완료 후 즉시 삭제 (일회성)

        return EmailVerificationResponseDto.builder()
                .success(true)
                .message("이메일 인증이 완료되었습니다.")
                .emailVerified(user.isEmailVerified())
                .build();
    }

    /**
     * 인증 이메일을 재발송합니다.
     *
     * @param request 인증 코드 재발송 요청 DTO
     */
    public void resendVerificationEmail(EmailResendRequestDto request) {
        User user = userRepository.findUnverifiedLocalUser(request.getEmail())
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND_OR_ALREADY_VERIFIED));

        // TODO: 재발송 쿨타임(rate limiting) 로직 추가

        sendVerificationEmail(user);
    }
}