package com.aid.train.backend.global.security.handler;

import com.aid.train.backend.domain.user.service.AuthService;
import com.aid.train.backend.domain.verification.dto.response.SocialCallbackResponseDto;
import com.aid.train.backend.global.util.CookieUtil;
import com.aid.train.backend.global.util.LogMaskingUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 처리 핸들러입니다.
 * 소셜 로그인 성공 후 사용자 정보를 처리하고 프론트엔드의 적절한 페이지로 리다이렉트합니다.
 *
 * <p>
 * **변경사항 (일회용 코드 방식 도입):**
 * - 기존 회원: AccessToken 대신 일회용 코드를 URL 파라미터로 전달
 * - 신규 회원: 기존과 동일하게 SOCIAL_SIGNUP_PENDING_TOKEN을 URL 파라미터로 전달
 * - 보안 강화: URL에 실제 토큰이 노출되지 않고 일회용 코드만 노출됨
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend.base-url:http://localhost:5050}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauth2Token.getPrincipal();
        String registrationId = oauth2Token.getAuthorizedClientRegistrationId();

        log.info("OAuth2 로그인 성공 - Provider: {}", registrationId);

        try {
            // 1. AuthService에 처리를 위임하고 결과를 받음
            SocialCallbackResponseDto callbackResponse = authService.processOAuth2User(registrationId, oauth2User);

            String targetUrl;

            if (callbackResponse.getIsNewUser()) {
                // --- 신규 회원 ---
                log.info("신규 소셜 사용자. 추가 정보 입력 페이지로 리다이렉트. Email: {}",
                        LogMaskingUtil.maskEmail(callbackResponse.getEmail()));
                // 프론트엔드의 추가 정보 입력 페이지로 리다이렉트, Pending Token을 쿼리 파라미터로 전달
                targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/social-signup")
                        .queryParam("token", callbackResponse.getSocialSignupPendingToken())
                        .build().toUriString();
            } else {
                // --- 기존 회원 ---
                log.info("기존 소셜 사용자. 메인 페이지로 리다이렉트. Email: {}",
                        LogMaskingUtil.maskEmail(callbackResponse.getEmail()));


                // **핵심 변경사항**: AccessToken 대신 일회용 코드를 URL 파라미터로 전달
                // 프론트엔드에서는 이 코드를 받아서 /api/v1/users/token/exchange API를 호출하여 AccessToken으로 교환해야 함
                targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/")
                        .queryParam("code", callbackResponse.getOneTimeCode())  // AccessToken → OneTimeCode 변경
                        .build().toUriString();

                log.debug("기존 회원 일회용 코드 리다이렉트 URL 생성 완료. Code: {}",
                        LogMaskingUtil.maskToken(callbackResponse.getOneTimeCode()));  // 접두어/**** 형태
            }

            // 2. 결정된 URL로 리다이렉트
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 실패 - Provider: {}, 오류: {}",
                    registrationId, LogMaskingUtil.maskSensitiveData(e.getMessage()), e);
            String errorUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/login")
                    .queryParam("error", "oauth_processing_failed")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}