package com.aid.train.backend.domain.user.service;

import com.aid.train.backend.domain.user.dto.request.LoginRequestDto;
import com.aid.train.backend.domain.user.dto.request.RefreshTokenRequestDto;
import com.aid.train.backend.domain.user.dto.request.SignupRequestDto;
import com.aid.train.backend.domain.user.dto.response.LoginResponseDto;
import com.aid.train.backend.domain.user.dto.response.SignupResponseDto;
import com.aid.train.backend.domain.user.dto.response.TokenResponseDto;
import com.aid.train.backend.domain.user.entity.RefreshToken;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.aid.train.backend.domain.user.repository.RefreshTokenRepository;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.entity.UserConsent;
import com.aid.train.backend.domain.terms.enums.TermsType;
import com.aid.train.backend.domain.terms.repository.TermsRepository;
import com.aid.train.backend.domain.terms.repository.UserConsentRepository;
import com.aid.train.backend.domain.verification.repository.EmailVerificationRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 인증/인가 서비스입니다.
 * 회원가입, 로그인, 토큰 관리 등의 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TermsRepository termsRepository;
    private final UserConsentRepository userConsentRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입을 처리합니다.
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     * @throws TrainException 이미 존재하는 이메일인 경우
     * @throws TrainException 이메일 인증이 완료되지 않은 경우
     */
    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {
        log.info("[회원가입] 회원가입 시작 - 이메일: {}", request.getEmail());

        // 1. 이메일 중복 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[회원가입] 이미 존재하는 이메일 - {}", request.getEmail());
            throw new TrainException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. 이메일 인증 완료 여부 검증
        var verification = emailVerificationRepository
                .findByEmailAndCode(request.getEmail(), request.getVerificationCode())
                .orElseThrow(() -> {
                    log.warn("[회원가입] 유효하지 않은 인증 코드 - 이메일: {}", request.getEmail());
                    return new TrainException(ErrorCode.INVALID_VERIFICATION_CODE);
                });

        if (!verification.getIsVerified()) {
            log.warn("[회원가입] 인증되지 않은 코드 - 이메일: {}", request.getEmail());
            throw new TrainException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 3. 사용자 생성
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.createLocalUser(
                request.getEmail(),
                encodedPassword,
                request.getNickname()
        );
        userRepository.save(user);
        log.info("[회원가입] 사용자 생성 완료 - ID: {}, 이메일: {}", user.getId(), user.getEmail());

        // 4. 약관 동의 처리
        saveUserConsents(user, request);

        // 5. 인증 완료 후 이메일 인증 정보 삭제
        emailVerificationRepository.deleteByEmail(request.getEmail());

        // 6. 토큰 발급
        TokenResponseDto token = issueTokens(user, null);

        log.info("[회원가입] 회원가입 완료 - 사용자 ID: {}", user.getId());
        return SignupResponseDto.of(user, token);
    }

    /**
     * 로그인을 처리합니다.
     *
     * @param request 로그인 요청 DTO
     * @return 로그인 응답 DTO
     * @throws TrainException 사용자를 찾을 수 없는 경우
     * @throws TrainException 비밀번호가 일치하지 않는 경우
     * @throws TrainException 탈퇴한 사용자인 경우
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        log.info("[로그인] 로그인 시도 - 이메일: {}", request.getEmail());

        // 1. 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("[로그인] 존재하지 않는 이메일 - {}", request.getEmail());
                    return new TrainException(ErrorCode.USER_NOT_FOUND);
                });

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("[로그인] 비밀번호 불일치 - 이메일: {}", request.getEmail());
            throw new TrainException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. 탈퇴 사용자 검증
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            log.warn("[로그인] 탈퇴한 사용자 로그인 시도 - 이메일: {}", request.getEmail());
            throw new TrainException(ErrorCode.USER_WITHDRAWN);
        }

        // 4. 휴면 계정 활성화 (정책 B)
        if (user.getStatus() == UserStatus.INACTIVE) {
            user.activateFromInactive();
            log.info("[로그인] 휴면 계정 활성화 - 사용자 ID: {}", user.getId());
        }

        // 5. 최종 로그인 시간 업데이트
        user.updateLastLoginAt();

        // 6. 토큰 발급
        TokenResponseDto token = issueTokens(user, request.getDeviceId());

        log.info("[로그인] 로그인 성공 - 사용자 ID: {}", user.getId());
        return LoginResponseDto.of(user, token);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰을 재발급합니다.
     *
     * @param request 토큰 갱신 요청 DTO
     * @return 새로운 토큰 응답 DTO
     * @throws TrainException 유효하지 않은 리프레시 토큰인 경우
     */
    @Transactional
    public TokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        log.info("[토큰 갱신] 토큰 갱신 시도");

        // 1. 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            log.warn("[토큰 갱신] 유효하지 않은 리프레시 토큰");
            throw new TrainException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. DB에서 리프레시 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> {
                    log.warn("[토큰 갱신] 존재하지 않는 리프레시 토큰");
                    return new TrainException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                });

        // 3. 폐기된 토큰 검증
        if (refreshToken.getRevoked()) {
            log.warn("[토큰 갱신] 폐기된 리프레시 토큰 - 사용자 ID: {}", refreshToken.getUser().getId());
            throw new TrainException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        // 4. 만료 검증
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("[토큰 갱신] 만료된 리프레시 토큰 - 사용자 ID: {}", refreshToken.getUser().getId());
            throw new TrainException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 5. 새로운 액세스 토큰 발급
        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        LocalDateTime accessTokenExpiryDate = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getAccessTokenValidityInSeconds());

        log.info("[토큰 갱신] 토큰 갱신 완료 - 사용자 ID: {}", user.getId());

        return TokenResponseDto.of(
                newAccessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getAccessTokenValidityInSeconds(),
                accessTokenExpiryDate,
                refreshToken.getExpiryDate()
        );
    }

    /**
     * 로그아웃을 처리합니다.
     * 해당 사용자의 모든 리프레시 토큰을 폐기합니다.
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void logout(Long userId) {
        log.info("[로그아웃] 로그아웃 시작 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        // 모든 리프레시 토큰 폐기
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevokedOrderByCreatedAtDesc(user, false);
        tokens.forEach(RefreshToken::revoke);

        log.info("[로그아웃] 로그아웃 완료 - 폐기된 토큰 개수: {}", tokens.size());
    }

    /**
     * 토큰을 발급합니다.
     *
     * @param user     사용자 엔티티
     * @param deviceId 디바이스 ID (선택)
     * @return 토큰 응답 DTO
     */
    private TokenResponseDto issueTokens(User user, String deviceId) {
        // 1. 액세스 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        LocalDateTime accessTokenExpiryDate = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getAccessTokenValidityInSeconds());

        // 2. 리프레시 토큰 생성
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId());
        LocalDateTime refreshTokenExpiryDate = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenValidityInSeconds());

        // 3. 기존 디바이스 토큰이 있으면 폐기
        if (deviceId != null) {
            refreshTokenRepository.findByUserAndDeviceId(user, deviceId)
                    .ifPresent(RefreshToken::revoke);
        }

        // 4. 리프레시 토큰 저장
        RefreshToken refreshToken = RefreshToken.createRefreshToken(
                user,
                refreshTokenValue,
                refreshTokenExpiryDate,
                deviceId
        );
        refreshTokenRepository.save(refreshToken);

        log.info("[토큰 발급] 토큰 발급 완료 - 사용자 ID: {}", user.getId());

        return TokenResponseDto.of(
                accessToken,
                refreshTokenValue,
                jwtTokenProvider.getAccessTokenValidityInSeconds(),
                accessTokenExpiryDate,
                refreshTokenExpiryDate
        );
    }

    /**
     * 사용자 약관 동의를 저장합니다.
     *
     * @param user    사용자 엔티티
     * @param request 회원가입 요청 DTO
     */
    private void saveUserConsents(User user, SignupRequestDto request) {
        // 1. 서비스 이용약관 동의
        if (request.getAgreeTermsOfService()) {
            Terms termsOfService = termsRepository
                    .findByTypeAndIsActive(TermsType.TERMS_OF_SERVICE, true)
                    .orElseThrow(() -> new TrainException(ErrorCode.TERMS_NOT_FOUND));

            UserConsent consent = UserConsent.createConsent(user, termsOfService, true);
            userConsentRepository.save(consent);
        }

        // 2. 개인정보 처리방침 동의
        if (request.getAgreePrivacyPolicy()) {
            Terms privacyPolicy = termsRepository
                    .findByTypeAndIsActive(TermsType.PRIVACY_POLICY, true)
                    .orElseThrow(() -> new TrainException(ErrorCode.TERMS_NOT_FOUND));

            UserConsent consent = UserConsent.createConsent(user, privacyPolicy, true);
            userConsentRepository.save(consent);
        }

        // 3. 마케팅 수신 동의 (선택)
        if (request.getAgreeMarketingConsent() != null && request.getAgreeMarketingConsent()) {
            Terms marketingConsent = termsRepository
                    .findByTypeAndIsActive(TermsType.MARKETING_CONSENT, true)
                    .orElseThrow(() -> new TrainException(ErrorCode.TERMS_NOT_FOUND));

            UserConsent consent = UserConsent.createConsent(user, marketingConsent, true);
            userConsentRepository.save(consent);
        }

        log.info("[약관 동의] 약관 동의 저장 완료 - 사용자 ID: {}", user.getId());
    }
}
