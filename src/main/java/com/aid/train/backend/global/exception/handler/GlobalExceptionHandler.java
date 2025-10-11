package com.aid.train.backend.global.exception.handler;

import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.exception.dto.ErrorResponse;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외처리를 담당하는 핸들러입니다.
 * 애플리케이션에서 발생하는 모든 예외처리를 일관된 형식으로 처리합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     *  애플리케이션에서 발생하는 커스텁 예외들을 처리합니다.
     *  @ExceptionHandler - 애플리케이션에서 throw된 에러들을 처리할 예외클래스
     */
    @ExceptionHandler(TrainException.class)
    public ResponseEntity<ErrorResponse> handleTrainException(TrainException e, HttpServletRequest req) {
        log.warn("애플리케이션 예외 발생: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .detail(e.getMessage())
                .path(req.getRequestURI())
                .status(e.getErrorCode().getStatus())
                .error(e.getErrorCode().getCode())
                .build();

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(errorResponse);
    }

    /**
     * 유효성 검사 실패 에러를 처리합니다.
     *  @Valid 유효성 검사 실패 처리 (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {

        List<ErrorResponse.ValidationError> validationErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> ErrorResponse.ValidationError.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .rejectedValue(fieldError.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        log.warn("유효성 검증 실패: {}", validationErrors);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .error(ErrorCode.VALIDATION_ERROR.getCode())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }


    /**
     * 로그인 실패 에러를 (비밀번호 불일치 등) 처리합니다. (401)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.warn("로그인 실패: {}", e.getMessage());
        return createErrorResponse(ErrorCode.INVALID_PASSWORD, ErrorCode.INVALID_PASSWORD.getMessage(), request);
    }

    /**
     * 필요한 권한이 없을 경우를 (인가 실패) 처리합니다. (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("접근 권한 없음: {}", e.getMessage());
        return createErrorResponse(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage(), request);
    }

    /**
     * 그 외 모든 예외를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("처리되지 않은 예외 발생", e);
        return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), request);
    }

    /**
     * 공통 에러 응답을 생성합니다.
     * @param errorCode - 400, 500 ... 등의 에러코드
     * @param message - 에러 발생 원인 메세지
     * @param req - 현재 HTTP 요청 정보
     * @return ResponseEntity - 에러 응답을 담은 객체
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(ErrorCode errorCode, String message, HttpServletRequest req) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus())
                .error(errorCode.getCode())
                .detail(message)
                .path(req.getRequestURI())
                .build();

        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }
}
