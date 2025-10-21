package com.aid.train.backend.domain.user.controller;

import com.aid.train.backend.domain.user.dto.request.ChangePasswordRequestDto;
import com.aid.train.backend.domain.user.dto.request.UpdateProfileRequestDto;
import com.aid.train.backend.domain.user.dto.response.UserProfileResponseDto;
import com.aid.train.backend.domain.user.service.UserService;
import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.global.security.annotation.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관리 API 컨트롤러입니다.
 * 프로필 조회/수정, 비밀번호 변경, 회원 탈퇴/복구 엔드포인트를 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Tag(name = "04. 사용자 관리", description = "사용자 프로필 및 계정 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 프로필을 조회합니다.
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 사용자 프로필 응답 DTO
     */
    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필을 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponseDto> getMyProfile(
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        log.info("[API] GET /api/v1/users/me - 사용자 ID: {}", userId);
        UserProfileResponseDto response = userService.getUserProfile(userId);
        return ApiResponse.ok(response);
    }

    /**
     * 프로필을 수정합니다.
     *
     * @param userId  현재 로그인한 사용자 ID
     * @param request 프로필 수정 요청 DTO
     * @return 수정된 프로필 응답 DTO
     */
    @Operation(summary = "프로필 수정", description = "닉네임 또는 프로필 이미지를 수정합니다.")
    @PatchMapping("/me")
    public ApiResponse<UserProfileResponseDto> updateProfile(
            @Parameter(hidden = true) @CurrentUserId Long userId,
            @Valid @RequestBody UpdateProfileRequestDto request
    ) {
        log.info("[API] PATCH /api/v1/users/me - 사용자 ID: {}", userId);
        UserProfileResponseDto response = userService.updateProfile(userId, request);
        return ApiResponse.ok(response, "프로필이 수정되었습니다.");
    }

    /**
     * 비밀번호를 변경합니다.
     *
     * @param userId  현재 로그인한 사용자 ID
     * @param request 비밀번호 변경 요청 DTO
     * @return 성공 응답
     */
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.")
    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @Parameter(hidden = true) @CurrentUserId Long userId,
            @Valid @RequestBody ChangePasswordRequestDto request
    ) {
        log.info("[API] PATCH /api/v1/users/me/password - 사용자 ID: {}", userId);
        userService.changePassword(userId, request);
        return ApiResponse.ok("비밀번호가 변경되었습니다.");
    }

    /**
     * 회원 탈퇴를 처리합니다.
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 성공 응답
     */
    @Operation(
            summary = "회원 탈퇴",
            description = "회원 탈퇴를 처리합니다. 탈퇴 후 30일 이내에 복구 가능합니다."
    )
    @DeleteMapping("/me")
    public ApiResponse<Void> withdrawUser(
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        log.info("[API] DELETE /api/v1/users/me - 사용자 ID: {}", userId);
        userService.withdrawUser(userId);
        return ApiResponse.ok("회원 탈퇴가 완료되었습니다. 30일 이내에 복구 가능합니다.");
    }

    /**
     * 탈퇴 철회를 처리합니다.
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 성공 응답
     */
    @Operation(
            summary = "탈퇴 철회",
            description = "탈퇴한 계정을 복구합니다. 탈퇴 후 30일 이내에만 가능합니다."
    )
    @PostMapping("/me/restore")
    public ApiResponse<Void> restoreUser(
            @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        log.info("[API] POST /api/v1/users/me/restore - 사용자 ID: {}", userId);
        userService.restoreUser(userId);
        return ApiResponse.ok("계정이 복구되었습니다.");
    }
}
