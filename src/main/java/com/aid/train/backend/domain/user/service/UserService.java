package com.aid.train.backend.domain.user.service;

import com.aid.train.backend.domain.user.dto.request.ChangePasswordRequestDto;
import com.aid.train.backend.domain.user.dto.request.UpdateProfileRequestDto;
import com.aid.train.backend.domain.user.dto.response.UserProfileResponseDto;
import com.aid.train.backend.domain.user.entity.SocialAccount;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.UserStatus;
import com.aid.train.backend.domain.user.repository.RefreshTokenRepository;
import com.aid.train.backend.domain.user.repository.SocialAccountRepository;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.terms.repository.UserConsentRepository;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 관리 서비스입니다.
 * 프로필 조회, 수정, 비밀번호 변경, 회원 탈퇴 등의 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserConsentRepository userConsentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 프로필을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 프로필 응답 DTO
     * @throws TrainException 사용자를 찾을 수 없는 경우
     */
    public UserProfileResponseDto getUserProfile(Long userId) {
        log.info("[프로필 조회] 프로필 조회 시작 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[프로필 조회] 사용자를 찾을 수 없음 - ID: {}", userId);
                    return new TrainException(ErrorCode.USER_NOT_FOUND);
                });

        List<SocialAccount> socialAccounts = socialAccountRepository
                .findByUserOrderByLastLoginAtDesc(user);

        log.info("[프로필 조회] 프로필 조회 완료 - 사용자 ID: {}", userId);
        return UserProfileResponseDto.of(user, socialAccounts);
    }

    /**
     * 사용자 프로필을 수정합니다.
     *
     * @param userId  사용자 ID
     * @param request 프로필 수정 요청 DTO
     * @return 수정된 프로필 응답 DTO
     * @throws TrainException 사용자를 찾을 수 없는 경우
     * @throws TrainException 잘못된 URL 형식인 경우
     */
    @Transactional
    public UserProfileResponseDto updateProfile(Long userId, UpdateProfileRequestDto request) {
        log.info("[프로필 수정] 프로필 수정 시작 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        // 1. 닉네임 수정
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.updateNickname(request.getNickname());
            log.info("[프로필 수정] 닉네임 변경 - 사용자 ID: {}, 새 닉네임: {}", userId, request.getNickname());
        }

        // 2. 프로필 이미지 수정
        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()) {
            // URL 형식 재검증 (방어적 프로그래밍)
            if (!isValidUrl(request.getProfileImageUrl())) {
                log.warn("[프로필 수정] 잘못된 URL 형식 - 사용자 ID: {}, URL: {}", 
                        userId, request.getProfileImageUrl());
                throw new TrainException(ErrorCode.INVALID_URL_FORMAT);
            }
            user.updateProfileImage(request.getProfileImageUrl());
            log.info("[프로필 수정] 프로필 이미지 변경 - 사용자 ID: {}", userId);
        }

        List<SocialAccount> socialAccounts = socialAccountRepository
                .findByUserOrderByLastLoginAtDesc(user);

        log.info("[프로필 수정] 프로필 수정 완료 - 사용자 ID: {}", userId);
        return UserProfileResponseDto.of(user, socialAccounts);
    }

    /**
     * 비밀번호를 변경합니다.
     *
     * @param userId  사용자 ID
     * @param request 비밀번호 변경 요청 DTO
     * @throws TrainException 사용자를 찾을 수 없는 경우
     * @throws TrainException 현재 비밀번호가 일치하지 않는 경우
     * @throws TrainException 새 비밀번호가 현재 비밀번호와 동일한 경우
     * @throws TrainException 소셜 로그인 사용자인 경우
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequestDto request) {
        log.info("[비밀번호 변경] 비밀번호 변경 시작 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        // 1. 소셜 로그인 사용자 검증 (비밀번호 없음)
        if (user.getPassword() == null) {
            log.warn("[비밀번호 변경] 소셜 로그인 사용자는 비밀번호 변경 불가 - 사용자 ID: {}", userId);
            throw new TrainException(ErrorCode.SOCIAL_USER_NO_PASSWORD);
        }

        // 2. 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("[비밀번호 변경] 현재 비밀번호 불일치 - 사용자 ID: {}", userId);
            throw new TrainException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. 새 비밀번호가 현재 비밀번호와 동일한지 검증
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            log.warn("[비밀번호 변경] 새 비밀번호가 현재 비밀번호와 동일 - 사용자 ID: {}", userId);
            throw new TrainException(ErrorCode.SAME_AS_CURRENT_PASSWORD);
        }

        // 4. 비밀번호 변경
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);

        log.info("[비밀번호 변경] 비밀번호 변경 완료 - 사용자 ID: {}", userId);
    }

    /**
     * 회원 탈퇴를 처리합니다.
     * 사용자 상태를 WITHDRAWN으로 변경하고 관련 데이터를 정리합니다.
     *
     * @param userId 사용자 ID
     * @throws TrainException 사용자를 찾을 수 없는 경우
     * @throws TrainException 이미 탈퇴한 사용자인 경우
     */
    @Transactional
    public void withdrawUser(Long userId) {
        log.info("[회원 탈퇴] 회원 탈퇴 시작 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        // 1. 이미 탈퇴한 사용자 검증
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            log.warn("[회원 탈퇴] 이미 탈퇴한 사용자 - 사용자 ID: {}", userId);
            throw new TrainException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }

        // 2. 사용자 상태 변경 (WITHDRAWN)
        user.withdraw();

        // 3. 모든 리프레시 토큰 삭제
        refreshTokenRepository.deleteByUser(user);
        log.info("[회원 탈퇴] 리프레시 토큰 삭제 완료 - 사용자 ID: {}", userId);

        // 4. 소셜 계정 연동 해제
        List<SocialAccount> socialAccounts = socialAccountRepository.findByUserOrderByLastLoginAtDesc(user);
        socialAccounts.forEach(SocialAccount::disconnect);
        log.info("[회원 탈퇴] 소셜 계정 연동 해제 완료 - 개수: {}", socialAccounts.size());

        // 5. 약관 동의 내역은 보관 (법적 요구사항)
        // 개인정보보호법에 따라 약관 동의 내역은 탈퇴 후에도 보관

        log.info("[회원 탈퇴] 회원 탈퇴 완료 - 사용자 ID: {}", userId);
    }

    /**
     * 탈퇴 철회를 처리합니다.
     * 탈퇴 후 30일 이내에만 가능합니다.
     *
     * @param userId 사용자 ID
     * @throws TrainException 사용자를 찾을 수 없는 경우
     * @throws TrainException 탈퇴한 사용자가 아닌 경우
     * @throws TrainException 복구 불가능한 경우 (30일 경과)
     */
    @Transactional
    public void restoreUser(Long userId) {
        log.info("[탈퇴 철회] 탈퇴 철회 시작 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        // 1. 탈퇴 상태 검증
        if (user.getStatus() != UserStatus.WITHDRAWN) {
            log.warn("[탈퇴 철회] 탈퇴한 사용자가 아님 - 사용자 ID: {}", userId);
            throw new TrainException(ErrorCode.USER_NOT_WITHDRAWN);
        }

        // 2. 복구 가능 여부 검증 (Entity에서 처리)
        if (!user.canRestore()) {
            log.warn("[탈퇴 철회] 복구 불가능 (30일 경과) - 사용자 ID: {}", userId);
            throw new TrainException(ErrorCode.CANNOT_RESTORE_USER);
        }

        // 3. 사용자 복구
        user.restore();

        log.info("[탈퇴 철회] 탈퇴 철회 완료 - 사용자 ID: {}", userId);
    }

    /**
     * URL 형식이 유효한지 검증합니다.
     *
     * @param url 검증할 URL
     * @return 유효하면 true, 아니면 false
     */
    private boolean isValidUrl(String url) {
        // http:// 또는 https://로 시작하는지 확인
        return url.matches("^https?://.+");
    }
}
