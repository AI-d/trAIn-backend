package com.aid.train.backend.domain.user.service;

import com.aid.train.backend.domain.terms.service.TermsService;
import com.aid.train.backend.domain.user.dto.request.PasswordChangeRequestDto;
import com.aid.train.backend.domain.user.dto.request.ProfileUpdateRequestDto;
import com.aid.train.backend.domain.user.dto.request.SignupRequestDto;
import com.aid.train.backend.domain.user.dto.response.SignupResponseDto;
import com.aid.train.backend.domain.user.dto.response.UserProfileResponseDto;
import com.aid.train.backend.domain.user.entity.User;
import com.aid.train.backend.domain.user.enums.Provider;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.verification.service.VerificationService;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 회원가입, 프로필 조회/수정, 비밀번호 변경 등의 기능을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final TermsService termsService;

    /**
     * 로컬 회원가입을 처리하고 결과를 DTO로 반환합니다.
     *
     * @param request 로컬 회원가입 요청 DTO
     * @return 회원가입 결과 DTO
     * @throws TrainException 비즈니스 규칙 위반 시
     */
    @Transactional
    public SignupResponseDto signUp(SignupRequestDto request) {
        if (!request.isPasswordMatched()) throw new TrainException(ErrorCode.PASSWORD_MISMATCH);
        if (request.isMinor()) throw new TrainException(ErrorCode.USER_AGE_RESTRICTION);
        if (!request.isJobDetailValid()) throw new TrainException(ErrorCode.JOB_DETAIL_REQUIRED);
        if (userRepository.existsByEmailAndPrimaryProvider(request.getEmail(), Provider.LOCAL)) {
            throw new TrainException(ErrorCode.USER_EMAIL_DUPLICATED);
        }
        termsService.validateConsents(request.getConsents());

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .jobType(request.getJobType())
                .jobDetail(request.getJobDetail())
                .primaryProvider(Provider.LOCAL)
                .emailVerified(false)
                .build();
        userRepository.saveAndFlush(newUser);

        termsService.saveUserConsents(newUser, request.getConsents());

        // VerificationService로부터 토큰을 받아옵니다.
        String token = verificationService.sendVerificationEmail(newUser);

        return SignupResponseDto.builder()
                .userId(newUser.getId())
                .email(newUser.getEmail())
                .name(newUser.getName())
                .emailVerified(newUser.getEmailVerified())
                .message("회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.")
                .emailVerificationToken(token)
                .build();
    }

    /**
     * 사용자 프로필을 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 사용자 프로필 정보 DTO
     * @throws TrainException 사용자를 찾을 수 없을 경우
     */
    public UserProfileResponseDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        return UserProfileResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .jobType(user.getJobType())
                .jobDetail(user.getJobDetail())
                .provider(user.getPrimaryProvider())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 사용자 프로필을 수정하고 수정된 프로필 정보를 반환합니다.
     *
     * @param userId  현재 로그인한 사용자의 ID
     * @param request 프로필 수정 요청 DTO
     * @return 수정된 사용자 프로필 정보 DTO
     * @throws TrainException 비즈니스 규칙 위반 시
     */
    @Transactional
    public UserProfileResponseDto updateProfile(Long userId, ProfileUpdateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));

        if (request.isMinor()) throw new TrainException(ErrorCode.USER_AGE_RESTRICTION);
        if (!request.isJobDetailValid()) throw new TrainException(ErrorCode.JOB_DETAIL_REQUIRED);

        user.updateName(request.getName());
        user.updateBirthDate(request.getBirthDate());
        user.updateJobInfo(request.getJobType(), request.getJobDetail());

        return getProfile(userId);
    }

    /**
     * 사용자의 비밀번호를 변경합니다.
     *
     * @param userId  현재 로그인한 사용자의 ID
     * @param request 비밀번호 변경 요청 DTO
     * @throws TrainException 비즈니스 규칙 위반 시
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TrainException(ErrorCode.USER_NOT_FOUND));
        if (user.isSocialAccount()) throw new TrainException(ErrorCode.SOCIAL_USER_PASSWORD_CHANGE_NOT_ALLOWED);
        if (!request.isNewPasswordMatched()) throw new TrainException(ErrorCode.PASSWORD_MISMATCH);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
            throw new TrainException(ErrorCode.CURRENT_PASSWORD_INVALID);
        if (request.isSameAsCurrentPassword()) throw new TrainException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }
}