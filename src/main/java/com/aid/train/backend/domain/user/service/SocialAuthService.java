package com.aid.train.backend.domain.user.service;

import com.aid.train.backend.domain.user.dto.request.SocialSignupRequestDto;
import com.aid.train.backend.domain.user.dto.response.LoginResponseDto;
import com.aid.train.backend.domain.user.dto.response.SocialLoginResponseDto;
import com.aid.train.backend.domain.user.entity.RefreshToken;
import com.aid.train.backend.domain.user.entity.SocialAccount;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.JobType;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.repository.RefreshTokenRepository;
import com.aid.train.backend.domain.user.repository.SocialAccountRepository;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.verification.entity.PendingSocialUser;
import com.aid.train.backend.domain.verification.repository.PendingSocialUserRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 소셜 로그인 서비스입니다.
 * OAuth2 인증 후처리 및 계정 연동을 담당합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SocialAuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PendingSocialUserRepository pendingSocialUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 소셜 로그인을 처리합니다.
     * 신규 사용자는 PendingSocialUser로 저장하고, 기존 사용자는 로그인 처리합니다.
     *
     * @param email         소셜 제공자의 이메일
     * @param name          소셜 제공자의 이름
     * @param provider      소셜 제공자 (KAKAO, GOOGLE, NAVER)
     * @param providerUserId 소셜 제공자의 사용자 ID
     * @return SocialLoginResponseDto
     */
    @Transactional
    public SocialLoginResponseDto processSocialLogin(
            String email,
            String name,
            Provider provider,
            String providerUserId
    ) {
        // 1. 소셜 계정으로 기존 User 조회
        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .orElse(null);

        if (socialAccount != null) {
            // 기존 사용자 로그인
            return handleExistingUser(socialAccount.getUser());
        }

        // 2. 이메일 + Provider로 User 조회
        User user = userRepository.findByEmailAndPrimaryProvider(email, provider)
                .orElse(null);

        if (user != null) {
            // 기존 사용자 + 새로운 소셜 계정 연동
            return handleSocialAccountLinking(user, provider, providerUserId);
        }

        // 3. 신규 사용자 - PendingSocialUser 생성
        return handleNewUser(email, name, provider, providerUserId);
    }

    /**
     * 소셜 회원가입 추가정보 입력을 완료하고 정식 User로 전환합니다.
     *
     * @param request 추가정보 요청
     * @return LoginResponseDto
     * @throws TrainException PendingSocialUser를 찾을 수 없거나 만료된 경우
     */
    @Transactional
    public LoginResponseDto completeSocialSignup(SocialSignupRequestDto request) {
        // jobType이 OTHER인 경우 jobDetail 필수 검증
        if (!request.isValid()) {
            throw new TrainException(ErrorCode.INVALID_INPUT_VALUE,
                    "직업 유형이 '기타'인 경우 직업 상세를 입력해야 합니다.");
        }

        // PendingSocialUser 조회
        PendingSocialUser pendingUser = pendingSocialUserRepository.findByTempToken(request.getTempToken())
                .orElseThrow(() -> new TrainException(ErrorCode.INVALID_TEMP_TOKEN));

        // 만료 확인
        if (pendingUser.isExpired()) {
            pendingSocialUserRepository.delete(pendingUser);
            throw new TrainException(ErrorCode.TEMP_TOKEN_EXPIRED);
        }

        // User 생성
        User user = User.createSocialUser(
                pendingUser.getEmail(),
                pendingUser.getName(),
                request.getBirthDate(),  // ✅ 필수
                request.getJobType(),     // ✅ 필수
                pendingUser.getProvider()
        );

        // jobDetail 설정 (jobType이 OTHER일 때만)
        if (request.getJobType() == JobType.OTHER) {
            user.updateJobInfo(request.getJobType(), request.getJobDetail());
        }

        userRepository.save(user);

        // SocialAccount 생성
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(pendingUser.getProvider())
                .providerUserId(pendingUser.getProviderUserId())
                .build();
        socialAccountRepository.save(socialAccount);

        // PendingSocialUser 삭제
        pendingSocialUserRepository.delete(pendingUser);

        // ✅ 마지막 로그인 시간 업데이트
        user.updateLastLogin();

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        saveRefreshToken(user.getId(), refreshToken);

        log.info("소셜 회원가입 완료 - userId: {}, email: {}, provider: {}",
                user.getId(), user.getEmail(), user.getPrimaryProvider());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    /**
     * 기존 사용자 로그인 처리
     */
    private SocialLoginResponseDto handleExistingUser(User user) {
        // 계정 상태 확인
        validateUserStatus(user);

        // ✅ 마지막 로그인 시간 업데이트
        user.updateLastLogin();

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        saveRefreshToken(user.getId(), refreshToken);

        log.info("소셜 로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());

        return SocialLoginResponseDto.builder()
                .isNewUser(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    /**
     * 기존 사용자 + 새로운 소셜 계정 연동
     */
    private SocialLoginResponseDto handleSocialAccountLinking(
            User user,
            Provider provider,
            String providerUserId
    ) {
        // SocialAccount 생성
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .build();
        socialAccountRepository.save(socialAccount);

        // ✅ 마지막 로그인 시간 업데이트
        user.updateLastLogin();

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        saveRefreshToken(user.getId(), refreshToken);

        log.info("소셜 계정 연동 및 로그인 - userId: {}, provider: {}", user.getId(), provider);

        return SocialLoginResponseDto.builder()
                .isNewUser(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    /**
     * 신규 사용자 - PendingSocialUser 생성
     */
    private SocialLoginResponseDto handleNewUser(
            String email,
            String name,
            Provider provider,
            String providerUserId
    ) {
        // 임시 토큰 생성
        String tempToken = UUID.randomUUID().toString();

        // PendingSocialUser 생성
        PendingSocialUser pendingUser = PendingSocialUser.builder()
                .email(email)
                .name(name)
                .provider(provider)
                .providerUserId(providerUserId)
                .tempToken(tempToken)
                .build();
        pendingSocialUserRepository.save(pendingUser);

        log.info("신규 소셜 사용자 - 추가정보 입력 필요, email: {}, provider: {}", email, provider);

        return SocialLoginResponseDto.builder()
                .isNewUser(true)
                .tempToken(tempToken)
                .email(email)
                .name(name)
                .provider(provider)
                .build();
    }

    /**
     * Refresh Token 저장
     */
    private void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByUserId(userId)
                .map(existingToken -> {
                    existingToken.updateToken(refreshToken);
                    return existingToken;
                })
                .orElseGet(() -> RefreshToken.builder()
                        .userId(userId)
                        .token(refreshToken)
                        .build());

        refreshTokenRepository.save(token);
    }

    /**
     * 계정 상태 검증
     */
    private void validateUserStatus(User user) {
        switch (user.getStatus()) {
            case INACTIVE:
                throw new TrainException(ErrorCode.INACTIVE_USER);
            case SUSPENDED:
                throw new TrainException(ErrorCode.SUSPENDED_USER);
            case WITHDRAWN:
                throw new TrainException(ErrorCode.WITHDRAWN_USER);
            case ACTIVE:
                break;
            default:
                throw new TrainException(ErrorCode.INVALID_USER_STATUS);
        }
    }
}