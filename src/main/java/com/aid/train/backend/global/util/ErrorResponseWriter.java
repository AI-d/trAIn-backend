package com.aid.train.backend.global.util;

import com.aid.train.backend.global.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * 에러 응답을 JSON 형식으로 작성하는 유틸리티 클래스입니다.
 * 인증/인가 실패 시 일관된 형식의 에러 응답을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
public class ErrorResponseWriter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * HTTP 응답에 에러 JSON을 작성합니다.
     *
     * @param request    HTTP 요청 객체
     * @param response   HTTP 응답 객체
     * @param statusCode HTTP 상태 코드
     * @param message    에러 메시지
     * @param errorCode  에러 코드
     * @throws IOException 응답 작성 실패 시
     */
    public static void write(
            HttpServletRequest request,
            HttpServletResponse response,
            int statusCode,
            String message,
            String errorCode
    ) throws IOException {

        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.error(errorCode, message);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

        log.warn("에러 응답 작성 - URI: {}, Status: {}, Code: {}, Message: {}",
                request.getRequestURI(), statusCode, errorCode, message);
    }
}