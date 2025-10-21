package com.aid.train.backend.global.security.handler;

import com.aid.train.backend.global.util.ErrorResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 처리하는 핸들러입니다.
 * HTTP 401 Unauthorized 응답을 반환합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        String errorMessage = "인증이 필요합니다";
        String requestUri = request.getRequestURI();

        // 인증 예외 타입별 세부 메시지 설정
        if (authException != null) {
            String exceptionMessage = authException.getMessage();
            if (exceptionMessage != null) {
                if (exceptionMessage.contains("expired")) {
                    errorMessage = "토큰이 만료되었습니다. 다시 로그인해주세요.";
                } else if (exceptionMessage.contains("malformed")) {
                    errorMessage = "잘못된 토큰 형식입니다.";
                } else if (exceptionMessage.contains("signature")) {
                    errorMessage = "유효하지 않은 토큰입니다.";
                }
            }
        }

        log.warn("인증 실패 - URI: {}, 에러: {}", requestUri,
                authException != null ? authException.getMessage() : "unknown");

        ErrorResponseWriter.write(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                errorMessage,
                "UNAUTHORIZED"
        );
    }
}