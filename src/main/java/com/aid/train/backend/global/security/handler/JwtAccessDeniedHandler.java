package com.aid.train.backend.global.security.handler;

import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.util.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 필요한 권한이 존재하지 않는 경우에 403 Forbidden 에러를 리턴하는 클래스입니다.
 * <p>
 * 인증은 되었지만 특정 리소스에 대한 접근 권한이 부족할 때 호출됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        String requestUri = request.getRequestURI();
        String errorMessage = errorCode.getMessage(); // 기본 메시지

        // 요청 경로에 따라 더 구체적인 에러 메시지를 설정할 수 있습니다. (선택적)

        log.warn("권한 부족 접근 거부: URI: {}, Principal: {}, Message: {}",
                requestUri,
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Anonymous",
                accessDeniedException.getMessage());

        ErrorResponseWriter.write(
                request,
                response,
                errorCode.getStatus(),
                errorMessage, // 상황에 맞게 커스터마이징된 메시지 사용
                errorCode.getCode()
        );
    }
}