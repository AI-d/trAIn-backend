package com.aid.train.backend.domain.user.service;

import com.aid.train.backend.domain.terms.service.TermsService;
import com.aid.train.backend.domain.user.dto.request.LoginRequestDto;
import com.aid.train.backend.domain.user.dto.request.SocialSignupCompleteRequestDto;
import com.aid.train.backend.domain.user.dto.response.LoginResponseDto;
import com.aid.train.backend.domain.user.dto.response.TokenRefreshResponseDto;
import com.aid.train.backend.domain.user.entity.RefreshToken;
import com.aid.train.backend.domain.user.entity.SocialAccount;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.aid.train.backend.domain.user.repository.RefreshTokenRepository;
import com.aid.train.backend.domain.user.repository.SocialAccountRepository;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.verification.dto.response.SocialCallbackResponseDto;
import com.aid.train.backend.domain.verification.entity.PendingSocialUser;
import com.aid.train.backend.domain.verification.repository.PendingSocialUserRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * 인증(Authentication) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 로컬 로그인, 토큰 발급/갱신, 로그아웃 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PendingSocialUserRepository pendingSocialUserRepository;
    private final TermsService termsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로컬 로그인을 처리하고 토큰 정보를 포함한 DTO를 반환합니다.
     *
     * @param request 로그인 요청 DTO
     * @return 로그인 응답 DTO
     * @throws TrainException 로그인 실패 시
     */
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findLoginableUser(request.getEmail(), Provider.LOCAL)
                .orElseThrow(() -> new TrainException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new TrainException(ErrorCode.LOGIN_FAILED);
        }
        if (!user.isEmailVerified()) {
            throw new TrainException(ErrorCode.USER_EMAIL_NOT_VERIFIED);
        }

        JwtTokenProvider.JwtResponse tokens = jwtTokenProvider.generateTokens(user.getId(), user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokens.getRefreshToken())
                .expiryDate(jwtTokenProvider.getExpiryDateTimeFromToken(tokens.getRefreshToken()))
                .build();
        refreshTokenRepository.save(refreshToken);

        user.updateLastLogin();
        if (!user.getStatus().isActive()) {
            user.updateStatus(UserStatus.ACTIVE);
        }

        return LoginResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .build();
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token 및 Refresh Token을 발급합니다. (Rotation 적용)
     *
     * @param refreshToken 사용자가 제출한 (이전) Refresh Token
     * @return 새로 발급된 Access Token과 Refresh Token을 담은 DTO
     * @throws TrainException Refresh Token이 유효하지 않거나 이미 사용된 경우
     */
    public TokenRefreshResponseDto refreshAccessToken(String refreshToken) {
        // 1. DB에서 이전 Refresh Token을 찾습니다. 없으면 유효하지 않거나 이미 사용된 토큰(탈취 시도)으로 간주.
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TrainException(ErrorCode.REFRESH_TOKEN_INVALID));

        // 2. 토큰 만료 여부 확인
        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new TrainException(ErrorCode.REFRESH_TOKEN_INVALID, "만료된 리프레시 토큰입니다.");
        }

        // 3. 이전 Refresh Token을 DB에서 즉시 삭제 (핵심: 재사용 방지)
        refreshTokenRepository.delete(storedToken);

        // 4. 이전 토큰에서 사용자 정보를 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        // 5. 새로운 Access Token과 새로운 Refresh Token을 생성
        JwtTokenProvider.JwtResponse newTokens = jwtTokenProvider.generateTokens(userId, email);

        // 6. 새로 생성된 Refresh Token을 DB에 저장
        saveRefreshToken(user, newTokens.getRefreshToken());

        // 7. 클라이언트에게 새로운 토큰들을 모두 반환
        return TokenRefreshResponseDto.builder()
                .accessToken(newTokens.getAccessToken())
                .refreshToken(newTokens.getRefreshToken()) // 새로운 Refresh Token도 함께 전달
                .build();
    }

    /**
     * 로그아웃을 처리합니다.
     *
     * @param refreshToken 무효화할 Refresh Token
     */
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * OAuth2 소셜 로그인 성공 후 사용자 정보를 처리합니다.
     * <p>
     * 1. 소셜 플랫폼에서 받은 사용자 정보(OAuth2User)를 파싱합니다.
     * 2. DB에서 해당 소셜 계정 정보로 기존 사용자를 조회합니다.
     * 3. (기존 회원) 즉시 로그인 처리 후 JWT 토큰을 발급합니다.
     * 4. (신규 회원) 회원가입 대기를 위한 임시 정보를 생성하고 SOCIAL_SIGNUP_PENDING_TOKEN을 발급합니다.
     * </p>
     *
     * @param registrationId 소셜 제공자 ID (e.g., "google", "kakao")
     * @param oAuth2User     소셜 플랫폼에서 받은 사용자 정보
     * @return 소셜 로그인 결과 DTO (신규/기존 여부, 토큰 정보 포함)
     */
    public SocialCallbackResponseDto processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        Provider provider = Provider.fromRegistrationId(registrationId);
        SocialUserInfo userInfo = extractSocialUserInfo(provider, oAuth2User);

        // 1. 기존 소셜 계정 조회
        Optional<SocialAccount> socialAccountOpt = socialAccountRepository
                .findByProviderAndProviderId(provider, userInfo.providerId());

        if (socialAccountOpt.isPresent()) {
            // --- 기존 회원인 경우 ---
            User user = socialAccountOpt.get().getUser();
            user.updateLastLogin();

            // JWT 토큰 발급
            JwtTokenProvider.JwtResponse tokens = jwtTokenProvider.generateTokens(user.getId(), user.getEmail());
            saveRefreshToken(user, tokens.getRefreshToken()); // Refresh Token 저장

            return SocialCallbackResponseDto.builder()
                    .isNewUser(false)
                    .accessToken(tokens.getAccessToken())
                    .refreshToken(tokens.getRefreshToken())
                    .email(user.getEmail())
                    .name(user.getName())
                    .provider(provider)
                    .build();
        } else {
            // --- 신규 회원인 경우 ---
            // 로컬 계정으로 이미 가입된 이메일인지 확인 (정책에 따라)
            if (userRepository.existsByEmailAndPrimaryProvider(userInfo.email(), Provider.LOCAL)) {
                throw new TrainException(ErrorCode.USER_EMAIL_DUPLICATED);
            }

            // SOCIAL_SIGNUP_PENDING_TOKEN 발급
            String pendingToken = jwtTokenProvider.generateSocialSignupPendingToken(
                    provider.name(), userInfo.providerId(), userInfo.email(), userInfo.name());

            PendingSocialUser pendingUser = PendingSocialUser.builder()
                    .pendingToken(pendingToken)
                    .provider(provider)
                    .providerId(userInfo.providerId())
                    .email(userInfo.email())
                    .name(userInfo.name())
                    .expiryDate(jwtTokenProvider.getExpiryDateTimeFromToken(pendingToken))
                    .build();
            pendingSocialUserRepository.save(pendingUser);

            return SocialCallbackResponseDto.builder()
                    .isNewUser(true)
                    .socialSignupPendingToken(pendingToken)
                    .email(userInfo.email())
                    .name(userInfo.name())
                    .provider(provider)
                    .build();
        }
    }

    /**
     * OAuth2User 객체에서 각 소셜 제공자에 맞는 사용자 정보를 추출합니다.
     *
     * @param provider   소셜 제공자 Enum
     * @param oAuth2User OAuth2User 객체
     * @return 추출된 사용자 정보를 담은 레코드
     */
    private SocialUserInfo extractSocialUserInfo(Provider provider, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        return switch (provider) {
            case GOOGLE -> new SocialUserInfo(
                    (String) attributes.get("sub"),
                    (String) attributes.get("email"),
                    (String) attributes.get("name")
            );
            case KAKAO -> {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                yield new SocialUserInfo(
                        String.valueOf(attributes.get("id")),
                        (String) kakaoAccount.get("email"),
                        (String) profile.get("nickname")
                );
            }
            case NAVER -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield new SocialUserInfo(
                        (String) response.get("id"),
                        (String) response.get("email"),
                        (String) response.get("name")
                );
            }
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 제공자입니다.");
        };
    }

    /**
     * Refresh Token을 DB에 저장하는 헬퍼 메서드
     */
    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(jwtTokenProvider.getExpiryDateTimeFromToken(token))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 소셜 사용자 정보를 담기 위한 내부 레코드
     */
    private record SocialUserInfo(String providerId, String email, String name) {
    }

    /**
     * 소셜 회원가입의 마지막 단계를 처리합니다.
     * <p>
     * 1. SOCIAL_SIGNUP_PENDING_TOKEN을 검증합니다.
     * 2. 사용자가 입력한 추가 정보(생년월일, 직업 등)와 약관 동의 내역을 검증합니다.
     * 3. User 및 SocialAccount 엔티티를 생성하고 DB에 저장합니다.
     * 4. 약관 동의 내역을 저장합니다.
     * 5. 사용 완료된 임시 토큰 정보를 삭제합니다.
     * 6. 최종 로그인 처리를 위해 JWT 토큰(Access/Refresh)을 발급하여 반환합니다.
     * </p>
     *
     * @param request 소셜 회원가입 완료 요청 DTO
     * @return 로그인 응답 DTO (토큰 포함)
     */
    @Transactional
    public LoginResponseDto completeSocialSignup(SocialSignupCompleteRequestDto request) {
        // 1. Pending Token 검증
        String pendingToken = request.getSocialSignupPendingToken();
        jwtTokenProvider.validateToken(pendingToken);

        PendingSocialUser pendingUser = pendingSocialUserRepository.findByPendingToken(pendingToken)
                .orElseThrow(() -> new TrainException(ErrorCode.SOCIAL_SIGNUP_PENDING_TOKEN_INVALID));

        if (pendingUser.getUsed() || pendingUser.isExpired()) {
            throw new TrainException(ErrorCode.SOCIAL_SIGNUP_PENDING_TOKEN_INVALID);
        }

        // 2. 입력값 및 약관 동의 검증
        if (request.isMinor()) throw new TrainException(ErrorCode.USER_AGE_RESTRICTION);
        if (!request.isJobDetailValid()) throw new TrainException(ErrorCode.JOB_DETAIL_REQUIRED);
        termsService.validateConsents(request.getConsents());

        // 3. User 및 SocialAccount 생성
        User newUser = User.builder()
                .email(pendingUser.getEmail())
                .name(pendingUser.getName())
                .birthDate(request.getBirthDate())
                .jobType(request.getJobType())
                .jobDetail(request.getJobDetail())
                .primaryProvider(pendingUser.getProvider())
                .emailVerified(true)
                .build();

        SocialAccount socialAccount = SocialAccount.builder()
                .provider(pendingUser.getProvider())
                .providerId(pendingUser.getProviderId())
                .socialEmail(pendingUser.getEmail())
                .socialName(pendingUser.getName())
                .build();

        // 연관관계 설정
        newUser.getSocialAccounts().add(socialAccount);
        socialAccount.setUser(newUser);

        userRepository.save(newUser);

        // 4. 약관 동의 내역 저장
        termsService.saveUserConsents(newUser, request.getConsents());

        // 5. 사용한 Pending 정보 처리
        pendingUser.markAsUsed();
        pendingSocialUserRepository.delete(pendingUser);

        // 6. 로그인 처리 및 토큰 발급
        JwtTokenProvider.JwtResponse tokens = jwtTokenProvider.generateTokens(newUser.getId(), newUser.getEmail());
        saveRefreshToken(newUser, tokens.getRefreshToken());

        return LoginResponseDto.builder()
                .userId(newUser.getId())
                .email(newUser.getEmail())
                .name(newUser.getName())
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .build();
    }
}