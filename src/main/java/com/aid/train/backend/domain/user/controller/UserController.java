package com.aid.train.backend.domain.user.controller;

import com.aid.train.backend.domain.user.dto.request.LoginRequestDto;
import com.aid.train.backend.domain.user.dto.request.PasswordChangeRequestDto;
import com.aid.train.backend.domain.user.dto.request.ProfileUpdateRequestDto;
import com.aid.train.backend.domain.user.dto.request.SignupRequestDto;
import com.aid.train.backend.domain.user.dto.response.LoginResponseDto;
import com.aid.train.backend.domain.user.dto.response.SignupResponseDto;
import com.aid.train.backend.domain.user.dto.response.TokenRefreshResponseDto;
import com.aid.train.backend.domain.user.dto.response.UserProfileResponseDto;
import com.aid.train.backend.domain.user.service.AuthService;
import com.aid.train.backend.domain.user.service.UserService;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.global.security.dto.CustomUserDetails;
import com.aid.train.backend.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User & Auth", description = "사용자 회원가입, 로그인, 프로필 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "로컬 회원가입", description = "이메일과 비밀번호를 사용하여 신규 사용자를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        SignupResponseDto response = userService.signUp(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.", response));
    }

    @Operation(summary = "로컬 로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        LoginResponseDto loginResponse = authService.login(loginRequestDto);
        cookieUtil.addRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", loginResponse));
    }

    @Operation(summary = "로그아웃", description = "Refresh Token 쿠키를 삭제하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        cookieUtil.getRefreshToken(request).ifPresent(authService::logout);
        cookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.success("성공적으로 로그아웃되었습니다.", null));
    }

    @Operation(summary = "Access Token 갱신", description = "...")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponseDto>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshToken(request)
                .orElseThrow(() -> new TrainException(ErrorCode.REFRESH_TOKEN_INVALID)); // 커스텀 예외로 변경

        TokenRefreshResponseDto responseDto = authService.refreshAccessToken(refreshToken);

        // 새로운 Refresh Token으로 쿠키를 업데이트해줍니다.
        cookieUtil.addRefreshTokenCookie(response, responseDto.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 갱신되었습니다.", responseDto));
    }

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponseDto profile = userService.getProfile(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("프로필 조회에 성공했습니다.", profile));
    }

    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequestDto profileUpdateRequestDto) {
        UserProfileResponseDto updatedProfile = userService.updateProfile(userDetails.getUserId(), profileUpdateRequestDto);
        return ResponseEntity.ok(ApiResponse.success("프로필이 성공적으로 수정되었습니다.", updatedProfile));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다. (로컬 계정 전용)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequestDto passwordChangeRequestDto) {
        userService.changePassword(userDetails.getUserId(), passwordChangeRequestDto);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다.", null));
    }
}