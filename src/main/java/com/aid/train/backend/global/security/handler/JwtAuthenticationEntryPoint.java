package com.aid.train.backend.global.security.handler;

import com.aid.train.backend.global.exception.enums.ErrorCode;
import com.aid.train.backend.global.util.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 유효한 자격증명을 제공하지 않고 접근하려 할 때 401 Unauthorized 에러를 리턴하는 클래스입니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        log.warn("인증 실패 응답: URI: {}, ErrorCode: {}, Message: {}",
                request.getRequestURI(), errorCode.getCode(), authException.getMessage());

        ErrorResponseWriter.write(
                request,
                response,
                errorCode.getStatus(),
                errorCode.getMessage(),
                errorCode.getCode()
        );
    }
}