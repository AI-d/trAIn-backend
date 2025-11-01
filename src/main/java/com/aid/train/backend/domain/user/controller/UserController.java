package com.aid.train.backend.domain.user.controller;

import com.aid.train.backend.domain.user.dto.request.ExchangeCodeRequestDto;
import com.aid.train.backend.domain.user.dto.request.LoginRequestDto;
import com.aid.train.backend.domain.user.dto.request.PasswordChangeRequestDto;
import com.aid.train.backend.domain.user.dto.request.ProfileUpdateRequestDto;
import com.aid.train.backend.domain.user.dto.request.SignupRequestDto;
import com.aid.train.backend.domain.user.dto.response.AccessTokenResponseDto;
import com.aid.train.backend.domain.user.dto.response.LoginResponseDto;
import com.aid.train.backend.domain.user.dto.response.SignupResponseDto;
import com.aid.train.backend.domain.user.dto.response.TokenRefreshResponseDto;
import com.aid.train.backend.domain.user.dto.response.UserProfileResponseDto;
import com.aid.train.backend.domain.user.service.AuthService;
import com.aid.train.backend.domain.user.service.UserService;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.dto.ErrorResponse;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.global.security.dto.CustomUserDetails;
import com.aid.train.backend.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

/**
 * 사용자 회원가입, 로그인, 프로필 관리 등 사용자 및 인증 관련 API 엔드포인트를 제공하는 컨트롤러입니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Tag(name = "User & Auth", description = "사용자 회원가입, 로그인, 프로필 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    /**
     * 사용자 관련 비즈니스 로직 처리 서비스
     */
    private final UserService userService;

    /**
     * 인증(로그인, 토큰 갱신 등) 관련 비즈니스 로직 처리 서비스
     */
    private final AuthService authService;

    /**
     * HttpOnly 쿠키 관리를 위한 유틸리티
     */
    private final CookieUtil cookieUtil;

    /**
     * 로컬 회원가입을 처리합니다.
     * 성공 시 이메일 인증 토큰을 Body로 반환합니다.
     *
     * @param signupRequestDto 회원가입 요청 정보 (이름, 이메일, 비밀번호 등)
     * @return 회원가입 결과 (사용자 ID, 이메일 인증 토큰 등 포함)
     */
    @Operation(summary = "로컬 회원가입", description = "이메일과 비밀번호를 사용하여 신규 사용자를 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = SignupResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패 (비밀번호 불일치, 나이 제한, 필수 약관 미동의 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "회원가입 요청 정보", required = true, content = @Content(schema = @Schema(implementation = SignupRequestDto.class)))
            @Valid @RequestBody SignupRequestDto signupRequestDto) {
        SignupResponseDto response = userService.signUp(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.", response));
    }

    /**
     * 로컬 로그인을 처리합니다.
     * 성공 시 AccessToken은 Body로, RefreshToken은 HttpOnly 쿠키로 발급합니다.
     *
     * @param loginRequestDto 로그인 요청 정보 (이메일, 비밀번호)
     * @param response        HttpServletResponse 객체 (쿠키 설정을 위함)
     * @return 로그인 결과 (AccessToken 등 포함, RefreshToken은 null)
     * @throws TrainException 로그인 실패 또는 이메일 미인증 시
     */
    @Operation(summary = "로컬 로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 실패 (자격 증명 불일치)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이메일 미인증 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "로그인 요청 정보", required = true, content = @Content(schema = @Schema(implementation = LoginRequestDto.class)))
            @Valid @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response) {
        LoginResponseDto loginResponse = authService.login(loginRequestDto);
        cookieUtil.addRefreshTokenCookie(response, loginResponse.getRefreshToken());

        // Body에서는 RefreshToken을 null로 설정 (API 계약 명확화)
        loginResponse.setRefreshToken(null);

        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", loginResponse));
    }

    /**
     * 로그아웃을 처리합니다.
     * HttpOnly 쿠키에 저장된 RefreshToken을 찾아 무효화하고 쿠키를 삭제합니다.
     *
     * @param request  HttpServletRequest 객체 (쿠키 조회를 위함)
     * @param response HttpServletResponse 객체 (쿠키 삭제를 위함)
     * @return 로그아웃 성공 메시지
     */
    @Operation(summary = "로그아웃", description = "Refresh Token 쿠키를 삭제하여 로그아웃 처리합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        cookieUtil.getRefreshToken(request).ifPresent(authService::logout);
        cookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.success("성공적으로 로그아웃되었습니다.", null));
    }

    /**
     * Access Token을 갱신합니다. (Refresh Token Rotation 적용)
     * HttpOnly 쿠키의 RefreshToken을 사용하여 새로운 AccessToken과 RefreshToken을 발급합니다.
     * 기존 RefreshToken은 무효화되고 새로운 RefreshToken이 발급됩니다.
     *
     * @param request  HttpServletRequest 객체 (쿠키 조회를 위함)
     * @param response HttpServletResponse 객체 (쿠키 갱신을 위함)
     * @return 갱신된 토큰 정보 (AccessToken 포함, RefreshToken은 null)
     * @throws TrainException RefreshToken이 없거나 유효하지 않을 때
     */
    @Operation(summary = "Access Token 갱신 (RTR)", description = "HttpOnly 쿠키의 RefreshToken을 사용하여 새로운 AccessToken과 RefreshToken을 발급받습니다. 기존 RefreshToken은 무효화됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(schema = @Schema(implementation = TokenRefreshResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh Token이 없거나 유효하지 않음 (만료, 이미 사용됨 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponseDto>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshToken(request)
                .orElseThrow(() -> new TrainException(ErrorCode.REFRESH_TOKEN_INVALID));

        TokenRefreshResponseDto responseDto = authService.refreshAccessToken(refreshToken);

        // 새로운 Refresh Token으로 쿠키를 업데이트
        cookieUtil.addRefreshTokenCookie(response, responseDto.getRefreshToken());

        // Body에서는 RefreshToken을 null로 설정 (보안)
        responseDto.setRefreshToken(null);

        return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 갱신되었습니다.", responseDto));
    }

    /**
     * 일회용 코드를 AccessToken과 RefreshToken으로 교환합니다.
     * 소셜 로그인 (기존 회원) 성공 직후 프론트엔드가 호출하는 API입니다.
     *
     * @param requestDto 교환할 일회용 코드를 포함한 DTO
     * @param response HttpServletResponse 객체 (쿠키 설정을 위함)
     * @return 발급된 AccessToken 정보 (RefreshToken은 HttpOnly 쿠키로 전달)
     * @throws TrainException 코드가 유효하지 않거나 만료된 경우
     */
    @Operation(summary = "일회용 코드-토큰 교환", description = "소셜 로그인 직후 받은 일회용 코드(URL 파라미터 'code')를 AccessToken(Body)과 RefreshToken(쿠키)으로 교환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 교환 성공", content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "일회용 코드가 유효하지 않음 (만료, 사용됨 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/token/exchange")
    public ResponseEntity<ApiResponse<LoginResponseDto>> exchangeToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "교환할 일회용 코드", required = true, content = @Content(schema = @Schema(implementation = ExchangeCodeRequestDto.class)))
            @Valid @RequestBody ExchangeCodeRequestDto requestDto,
            HttpServletResponse response) {

        LoginResponseDto loginResponse = authService.exchangeCodeForTokens(requestDto.code());

        // RefreshToken을 HttpOnly 쿠키로 설정
        cookieUtil.addRefreshTokenCookie(response, loginResponse.getRefreshToken());

        // Body에서는 RefreshToken을 null로 설정 (API 계약 명확화)
        loginResponse.setRefreshToken(null);

        // AccessToken과 사용자 정보를 JSON Body로 반환
        return ResponseEntity.ok(ApiResponse.success(
                "토큰 교환에 성공했습니다.",
                loginResponse
        ));
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회합니다.
     * AccessToken을 통한 인증이 필요합니다.
     *
     * @param userDetails 현재 인증된 사용자 정보 (@AuthenticationPrincipal 주입)
     * @return 사용자 프로필 정보
     */
    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 조회 성공", content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (AccessToken 누락/만료/위조)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponseDto profile = userService.getProfile(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("프로필 조회에 성공했습니다.", profile));
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 수정합니다.
     * AccessToken을 통한 인증이 필요합니다.
     *
     * @param userDetails              현재 인증된 사용자 정보 (@AuthenticationPrincipal 주입)
     * @param profileUpdateRequestDto 수정할 프로필 정보
     * @return 수정된 사용자 프로필 정보
     */
    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공", content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 프로필 정보", required = true, content = @Content(schema = @Schema(implementation = ProfileUpdateRequestDto.class)))
            @Valid @RequestBody ProfileUpdateRequestDto profileUpdateRequestDto) {
        UserProfileResponseDto updatedProfile = userService.updateProfile(userDetails.getUserId(), profileUpdateRequestDto);
        return ResponseEntity.ok(ApiResponse.success("프로필이 성공적으로 수정되었습니다.", updatedProfile));
    }

    /**
     * 현재 로그인한 사용자의 비밀번호를 변경합니다. (로컬 계정 전용)
     * AccessToken을 통한 인증이 필요합니다.
     *
     * @param userDetails                 현재 인증된 사용자 정보 (@AuthenticationPrincipal 주입)
     * @param passwordChangeRequestDto 비밀번호 변경 요청 정보
     * @return 비밀번호 변경 결과 메시지
     */
    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다. (로컬 계정 전용)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패 또는 현재 비밀번호 불일치", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "소셜 로그인 계정 (비밀번호 변경 불가)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "비밀번호 변경 요청 정보", required = true, content = @Content(schema = @Schema(implementation = PasswordChangeRequestDto.class)))
            @Valid @RequestBody PasswordChangeRequestDto passwordChangeRequestDto) {
        userService.changePassword(userDetails.getUserId(), passwordChangeRequestDto);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다.", null));
    }
}
