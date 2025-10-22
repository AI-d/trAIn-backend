package com.aid.train.backend.global.response;

import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 클라이언트에게 일관적인 응답 포맷을 제공하기 위한 객체입니다.
 * (두 버전의 장점을 병합한 버전)
 *
 * <p>사용 예시:</p>
 * <pre>
 * // Controller에서의 사용 (성공)
 * @GetMapping("/users/{id}")
 * public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
 * UserDto user = userService.getUserById(id);
 * return ResponseEntity.ok()
 * .body(ApiResponse.ok(user, "사용자 조회 성공"));
 * }
 *
 * // Controller에서의 사용 (실패)
 * @GetMapping("/users/error")
 * public ResponseEntity<ApiResponse<Void>> getError() {
 * // ...
 * return ResponseEntity.status(HttpStatus.NOT_FOUND)
 * .body(ApiResponse.error(ErrorCode.USER_NOT_FOUND));
 * }
 * </pre>
 *
 * @author 왕택준 (Merged by Gemini)
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    // 응답 성공 여부
    private boolean success;

    // 응답 메세지
    private String message;

    // 응답 시간
    private LocalDateTime timestamp;

    // 응답 Json
    private T data;

    // 에러 코드 (에러 응답 시에만 사용)
    private String errorCode;


    // ====================== 성공 응답 (기본) ======================

    /**
     * API 성공 응답을 생성합니다. (기본)
     *
     * @param message API 응답 메시지
     * @param data    응답 데이터 (모든 타입 가능)
     * @param <T>     응답 데이터의 타입
     * @return ApiResponse 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                // .errorCode(null) // 빌더 기본값이 null이므로 생략 가능
                .build();
    }

    // ====================== 성공 응답 (편의 메서드 - 200 OK) ======================

    /**
     * 성공 응답 (데이터만)
     * 사용: ApiResponse.ok(data)
     *
     * @param data 응답 데이터
     * @param <T>  응답 데이터의 타입
     * @return ApiResponse 성공 응답 객체
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("요청이 성공적으로 처리되었습니다.")
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    /**
     * 성공 응답 (데이터 + 메시지)
     * 사용: ApiResponse.ok(data, message)
     *
     * @param data    응답 데이터
     * @param message 응답 메시지
     * @param <T>     응답 데이터의 타입
     * @return ApiResponse 성공 응답 객체
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    /**
     * 성공 응답 (메시지만)
     * 사용: ApiResponse.ok(message)
     *
     * @param message 응답 메시지
     * @return ApiResponse 성공 응답 객체 (Data: null)
     */
    public static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    // ====================== 성공 응답 (편의 메서드 - 201 Created) ======================

    /**
     * 생성 성공 응답 (데이터 + 메시지)
     * 사용: ApiResponse.created(data, message)
     *
     * @param data    응답 데이터
     * @param message 응답 메시지
     * @param <T>     응답 데이터의 타입
     * @return ApiResponse 성공 응답 객체
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    /**
     * 생성 성공 응답 (데이터만)
     * 사용: ApiResponse.created(data)
     *
     * @param data 응답 데이터
     * @param <T>  응답 데이터의 타입
     * @return ApiResponse 성공 응답 객체
     */
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("리소스가 성공적으로 생성되었습니다.")
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }


    // ====================== 실패/에러 응답 (편의 메서드) ======================

    /**
     * API 에러 응답 (ErrorCode 기반)
     *
     * @param errorCode 에러 코드 enum
     * @return ApiResponse 에러 응답 객체 (Data: null)
     */
    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(errorCode.getMessage())
                .errorCode(errorCode.getCode()) // errorCode 필드 설정
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    /**
     * API 에러 응답 (ErrorCode + 커스텀 메시지)
     *
     * @param errorCode     에러 코드 enum
     * @param customMessage 커스텀 에러 메시지
     * @return ApiResponse 에러 응답 객체 (Data: null)
     */
    public static ApiResponse<Void> error(ErrorCode errorCode, String customMessage) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(customMessage) // 커스텀 메시지 사용
                .errorCode(errorCode.getCode()) // errorCode 필드 설정
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    /**
     * API 에러 응답 (메시지만 사용)
     *
     * @param message 에러 메시지
     * @return ApiResponse 에러 응답 객체 (Data: null)
     */
    public static ApiResponse<Void> error(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(null)
                .errorCode(null) // 특정 코드가 없음
                .build();
    }

    /**
     * API 에러 응답 (문자열 코드 + 메시지 사용)
     *
     * @param code    에러 코드 문자열
     * @param message 사용자 정의 에러 메시지
     * @return ApiResponse 에러 응답 객체 (Data: null)
     */
    public static ApiResponse<Void> error(String code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(code) // 커스텀 코드 설정
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }
}