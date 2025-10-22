package com.aid.train.backend.global.util;

import com.aid.train.backend.global.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * 에러 응답을 JSON 형식으로 작성하는 유틸리티 클래스입니다. (리팩토링 버전)
 * 인증/인가 실패 시 일관된 형식의 에러 응답을 제공하며, LocalDateTime 직렬화를 지원합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
public final class ErrorResponseWriter {

    private static final ObjectMapper objectMapper;

    // static 초기화 블록을 사용하여 ObjectMapper에 JavaTimeModule을 명시적으로 등록
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // 유틸리티 클래스의 인스턴스화 방지
    private ErrorResponseWriter() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

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

        // 우리 프로젝트의 ApiResponse.error 메서드를 사용
        ApiResponse<Void> errorResponse = ApiResponse.error(errorCode, message);

        try {
            // getWriter() 대신 getOutputStream()을 사용하여 직렬화
            objectMapper.writeValue(response.getOutputStream(), errorResponse);

            log.warn("에러 응답 작성 - URI: {}, Status: {}, Code: {}, Message: {}",
                    request.getRequestURI(), statusCode, errorCode, message);

        } catch (IOException e) {
            log.error("에러 응답 작성 실패 - Path: {}, Error: {}", request.getRequestURI(), e.getMessage());
            throw e; // 예외를 다시 던져서 서블릿 컨테이너가 처리하도록 함
        }
    }
}