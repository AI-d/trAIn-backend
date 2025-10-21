package com.aid.train.backend.domain.user.service;

import com.aid.train.backend.domain.user.dto.request.LoginRequestDto;
import com.aid.train.backend.domain.user.dto.request.RefreshTokenRequestDto;
import com.aid.train.backend.domain.user.dto.request.SignupRequestDto;
import com.aid.train.backend.domain.user.dto.response.LoginResponseDto;
import com.aid.train.backend.domain.user.dto.response.SignupResponseDto;
import com.aid.train.backend.domain.user.dto.response.TokenResponseDto;
import com.aid.train.backend.domain.user.entity.RefreshToken;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.repository.RefreshTokenRepository;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스입니다.
 * 회원가입, 로그인, 토큰 갱신 등을 처리합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로컬 회원가입을 처리합니다.
     *
     * @param request 회원가입 요청
     * @return SignupResponseDto
     * @throws TrainException 이메일이 이미 존재하거나 jobType 검증 실패
     */
    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {
        // jobType이 OTHER인 경우 jobDetail 필수 검증
        if (!request.isValid()) {
            throw new TrainException(ErrorCode.INVALID_INPUT_VALUE,
                    "직업 유형이 '기타'인 경우 직업 상세를 입력해야 합니다.");
        }

        // 이메일 중복 확인 (같은 Provider 내에서만)
        if (userRepository.existsByEmailAndPrimaryProvider(request.getEmail(), Provider.LOCAL)) {
            throw new TrainException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 생성
        User user = User.createLocalUser(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getBirthDate()  // ✅ 필수
        );

        // 직업 정보 설정 (선택)
        if (request.getJobType() != null) {
            user.updateJobInfo(request.getJobType(), request.getJobDetail());
        }

        userRepository.save(user);

        log.info("로컬 회원가입 완료 - email: {}, provider: LOCAL", request.getEmail());

        return SignupResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .message("회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.")
                .build();
    }

    /**
     * 로그인을 처리합니다.
     *
     * @param request 로그인 요청
     * @return LoginResponseDto (Access Token, Refresh Token 포함)
     * @throws TrainException 이메일 또는 비밀번호 불일치, 계정 비활성화
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        // 사용자 조회 (LOCAL Provider)
        User user = userRepository.findByEmailAndPrimaryProvider(request.getEmail(), Provider.LOCAL)
                .orElseThrow(() -> new TrainException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new TrainException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 계정 상태 확인
        validateUserStatus(user);

        // 이메일 인증 확인
        if (!user.isEmailVerified()) {
            throw new TrainException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // ✅ 마지막 로그인 시간 업데이트
        user.updateLastLogin();

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Refresh Token 저장
        saveRefreshToken(user.getId(), refreshToken);

        log.info("로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    /**
     * Refresh Token으로 Access Token을 재발급합니다.
     *
     * @param request Refresh Token 요청
     * @return TokenResponseDto (새로운 Access Token)
     * @throws TrainException 유효하지 않은 Refresh Token
     */
    @Transactional
    public TokenResponseDto refreshAccessToken(RefreshTokenRequestDto request) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new TrainException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Refresh Token에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());

        // 저장된 Refresh Token 확인
        RefreshToken storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedToken.getToken().equals(request.getRefreshToken())) {
            throw new TrainException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        // 계정 상태 확인
        validateUserStatus(user);

        // 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

        log.info("Access Token 재발급 완료 - userId: {}", userId);

        return TokenResponseDto.builder()
                .accessToken(newAccessToken)
                .build();
    }

    /**
     * 로그아웃을 처리합니다.
     * Refresh Token을 삭제합니다.
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("로그아웃 완료 - userId: {}", userId);
    }

    /**
     * Refresh Token을 저장합니다.
     * 기존 토큰이 있으면 업데이트합니다.
     *
     * @param userId       사용자 ID
     * @param refreshToken Refresh Token
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
     * 계정 상태를 검증합니다.
     *
     * @param user User 엔티티
     * @throws TrainException 계정이 비활성화, 정지, 탈퇴된 경우
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
                // 정상 상태
                break;
            default:
                throw new TrainException(ErrorCode.INVALID_USER_STATUS);
        }
    }
}