package com.aid.train.backend.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    // 에러 발생 시각
    private LocalDateTime timestamp;

    // http status code
    private int status;

    // 에러 코드 이름
    private String error;

    // 상세 에러 정보
    private String detail;

    // 에러가 발생한 URL 경로
    private String path;

    // 유효성 검증 에러 목록
    private List<ValidationError> validationErrors;

    /**
     * 입력값 검증 오류 1개를 포장할 내부 클래스입니다.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {

        // 에러가 난 필드면
        private String field;

        // 에러 원인 메세지
        private String message;

        // 거부된 값
        private Object rejectedValue;
    }
}
