package com.aid.train.backend.global.security.handler;

import com.aid.train.backend.global.util.ErrorResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증된 사용자가 권한이 없는 리소스에 접근할 때 처리하는 핸들러입니다.
 * HTTP 403 Forbidden 응답을 반환합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        String requestUri = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");

        log.warn("인가 실패 - URI: {}, 에러: {}, User-Agent: {}",
                requestUri,
                accessDeniedException.getMessage(),
                userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "unknown");

        // 요청 경로에 따른 세부 메시지 커스터마이징
        String errorMessage = "접근 권한이 없습니다";

        if (requestUri.contains("/admin")) {
            errorMessage = "관리자 권한이 필요합니다";
        } else if (requestUri.contains("/api/users/") && !requestUri.endsWith("/me")) {
            errorMessage = "다른 사용자의 정보에 접근할 수 없습니다";
        }

        ErrorResponseWriter.write(
                request,
                response,
                HttpServletResponse.SC_FORBIDDEN,
                errorMessage,
                "FORBIDDEN"
        );
    }
}