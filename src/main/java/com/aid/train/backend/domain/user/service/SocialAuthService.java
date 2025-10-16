package com.aid.train.backend.domain.user.service;

import com.aid.train.backend.domain.user.dto.request.SocialLoginRequestDto;
import com.aid.train.backend.domain.user.dto.request.SocialSignupRequestDto;
import com.aid.train.backend.domain.user.dto.response.SignupResponseDto;
import com.aid.train.backend.domain.user.dto.response.SocialLoginResponseDto;
import com.aid.train.backend.domain.user.dto.response.TokenResponseDto;
import com.aid.train.backend.domain.user.entity.RefreshToken;
import com.aid.train.backend.domain.user.entity.SocialAccount;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.aid.train.backend.domain.user.repository.RefreshTokenRepository;
import com.aid.train.backend.domain.user.repository.SocialAccountRepository;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.terms.entity.Terms;
import com.aid.train.backend.domain.terms.entity.UserConsent;
import com.aid.train.backend.domain.terms.enums.TermsType;
import com.aid.train.backend.domain.terms.repository.TermsRepository;
import com.aid.train.backend.domain.terms.repository.UserConsentRepository;
import com.aid.train.backend.domain.verification.entity.PendingSocialUser;
import com.aid.train.backend.domain.verification.repository.PendingSocialUserRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 소셜 로그인 서비스입니다.
 * OAuth 기반 소셜 로그인 및 회원가입 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SocialAuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PendingSocialUserRepository pendingSocialUserRepository;
    private final TermsRepository termsRepository;
    private final UserConsentRepository userConsentRepository;
    private final JwtTokenProvider jwtTokenProvider;
    // private final GoogleOAuthClient googleOAuthClient; // TODO: OAuth 클라이언트 구현 시 주입
    // private final KakaoOAuthClient kakaoOAuthClient;
    // private final NaverOAuthClient naverOAuthClient;

    /**
     * 소셜 로그인을 처리합니다.
     * 기존 사용자인 경우 로그인, 신규 사용자인 경우 임시 토큰을 발급합니다.
     *
     * @param request 소셜 로그인 요청 DTO
     * @return 소셜 로그인 응답 DTO
     */
    @Transactional
    public SocialLoginResponseDto socialLogin(SocialLoginRequestDto request) {
        log.info("[소셜 로그인] 소셜 로그인 시작 - 제공자: {}", request.getProvider());

        // 1. OAuth 제공자로부터 사용자 정보 가져오기
        // TODO: 실제 OAuth 클라이언트 구현 시 주석 해제
        SocialUserInfo socialUserInfo = fetchSocialUserInfo(request);

        // 2. 기존 소셜 계정 조회
        return socialAccountRepository
                .findByProviderAndProviderId(request.getProvider(), socialUserInfo.providerId())
                .map(socialAccount -> handleExistingUser(socialAccount, request.getDeviceId()))
                .orElseGet(() -> handleNewUser(socialUserInfo, request.getProvider()));
    }

    /**
     * 소셜 회원가입을 처리합니다.
     * 임시 토큰으로 대기 중인 사용자 정보를 조회하여 회원가입을 완료합니다.
     *
     * @param request 소셜 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     * @throws TrainException 유효하지 않은 임시 토큰인 경우
     */
    @Transactional
    public SignupResponseDto socialSignup(SocialSignupRequestDto request) {
        log.info("[소셜 회원가입] 소셜 회원가입 시작 - 임시 토큰: {}", request.getTempToken());

        // 1. 임시 사용자 정보 조회
        PendingSocialUser pendingUser = pendingSocialUserRepository
                .findByTempToken(request.getTempToken())
                .orElseThrow(() -> {
                    log.warn("[소셜 회원가입] 유효하지 않은 임시 토큰");
                    return new TrainException(ErrorCode.INVALID_TEMP_TOKEN);
                });

        // 2. 사용자 생성
        User user = User.createSocialUser(
                pendingUser.getEmail(),
                request.getNickname(),
                pendingUser.getProfileImageUrl()
        );
        userRepository.save(user);
        log.info("[소셜 회원가입] 사용자 생성 완료 - ID: {}, 이메일: {}", user.getId(), user.getEmail());

        // 3. 소셜 계정 연동
        SocialAccount socialAccount = SocialAccount.createSocialAccount(
                user,
                pendingUser.getProvider(),
                pendingUser.getProviderId()
        );
        socialAccountRepository.save(socialAccount);

        // 4. 약관 동의 처리
        saveUserConsents(user, request);

        // 5. 임시 사용자 정보 완료 처리
        pendingUser.complete();

        // 6. 토큰 발급
        TokenResponseDto token = issueTokens(user, null);

        log.info("[소셜 회원가입] 소셜 회원가입 완료 - 사용자 ID: {}", user.getId());
        return SignupResponseDto.of(user, token);
    }

    /**
     * 기존 사용자 로그인을 처리합니다.
     *
     * @param socialAccount 소셜 계정 엔티티
     * @param deviceId      디바이스 ID
     * @return 소셜 로그인 응답 DTO
     */
    private SocialLoginResponseDto handleExistingUser(SocialAccount socialAccount, String deviceId) {
        User user = socialAccount.getUser();
        log.info("[소셜 로그인] 기존 사용자 로그인 - 사용자 ID: {}", user.getId());

        // 1. 탈퇴 사용자 검증
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            log.warn("[소셜 로그인] 탈퇴한 사용자 로그인 시도 - 이메일: {}", user.getEmail());
            throw new TrainException(ErrorCode.USER_WITHDRAWN);
        }

        // 2. 휴면 계정 활성화 (정책 B)
        if (user.getStatus() == UserStatus.INACTIVE) {
            user.activateFromInactive();
            log.info("[소셜 로그인] 휴면 계정 활성화 - 사용자 ID: {}", user.getId());
        }

        // 3. 소셜 계정 로그인 시간 업데이트
        socialAccount.updateLastLogin();
        user.updateLastLoginAt();

        // 4. 토큰 발급
        TokenResponseDto token = issueTokens(user, deviceId);

        return SocialLoginResponseDto.ofExistingUser(user, socialAccount.getProvider(), token);
    }

    /**
     * 신규 사용자를 처리합니다.
     * 임시 토큰을 발급하여 추가 정보 입력을 유도합니다.
     *
     * @param socialUserInfo 소셜 사용자 정보
     * @param provider       소셜 제공자
     * @return 소셜 로그인 응답 DTO
     */
    private SocialLoginResponseDto handleNewUser(SocialUserInfo socialUserInfo, Provider provider) {
        log.info("[소셜 로그인] 신규 사용자 - 이메일: {}", socialUserInfo.email());

        // 1. 임시 토큰 생성
        String tempToken = UUID.randomUUID().toString();

        // 2. 임시 사용자 정보 저장
        PendingSocialUser pendingUser = PendingSocialUser.createPendingUser(
                provider,
                socialUserInfo.providerId(),
                socialUserInfo.email(),
                socialUserInfo.profileImageUrl(),
                tempToken
        );
        pendingSocialUserRepository.save(pendingUser);

        log.info("[소셜 로그인] 신규 사용자 임시 토큰 발급 - 임시 토큰: {}", tempToken);

        return SocialLoginResponseDto.ofNewUser(
                socialUserInfo.email(),
                provider,
                socialUserInfo.profileImageUrl(),
                tempToken
        );
    }

    /**
     * OAuth 제공자로부터 사용자 정보를 가져옵니다.
     *
     * @param request 소셜 로그인 요청 DTO
     * @return 소셜 사용자 정보
     */
    private SocialUserInfo fetchSocialUserInfo(SocialLoginRequestDto request) {
        // TODO: 실제 OAuth 클라이언트 구현
        // return switch (request.getProvider()) {
        //     case GOOGLE -> googleOAuthClient.getUserInfo(request.getAuthorizationCode(), request.getRedirectUri());
        //     case KAKAO -> kakaoOAuthClient.getUserInfo(request.getAuthorizationCode(), request.getRedirectUri());
        //     case NAVER -> naverOAuthClient.getUserInfo(request.getAuthorizationCode(), request.getRedirectUri());
        // };

        // 임시 더미 데이터 (개발용)
        log.warn("[소셜 로그인] OAuth 클라이언트 미구현 - 더미 데이터 사용");
        return new SocialUserInfo(
                "12345678",
                "test@example.com",
                "https://example.com/profile.jpg"
        );
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
     * @param request 소셜 회원가입 요청 DTO
     */
    private void saveUserConsents(User user, SocialSignupRequestDto request) {
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

    /**
     * 소셜 사용자 정보 레코드입니다.
     *
     * @param providerId      제공자의 사용자 고유 ID
     * @param email           이메일
     * @param profileImageUrl 프로필 이미지 URL
     */
    private record SocialUserInfo(
            String providerId,
            String email,
            String profileImageUrl
    ) {
    }
}
