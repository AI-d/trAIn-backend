package com.aid.train.backend.global.response;

import com.aid.train.backend.global.exception.enums.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 클라이언트에게 일관적인 응답 포맷을 제공하기 위한 객체입니다.
 *
 * <p>사용 예시:</p>
 * <pre>
 * // Controller에서의 사용
 * @GetMapping("/users/{id}")
 * public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
 *     UserDto user = userService.getUserById(id);
 *     return ResponseEntity.ok()
 *                          .body(ApiResponse.success("사용자 조회 성공", user));
 * }
 * </pre>
 *
 * @author 왕택준
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

    // ====================== 기본 메서드 ======================

    /**
     * API 성공 응답을 생성합니다.
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
                .build();
    }

    // ====================== 편의 메서드 (Controller 호환용) ======================

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
     * @return ApiResponse 성공 응답 객체
     */
    public static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }

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

    /**
     * 실패 응답
     * 사용: ApiResponse.fail(message)
     *
     * @param message 응답 메시지
     * @param <T>     응답 데이터의 타입
     * @return ApiResponse 실패 응답 객체
     */
    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    /**
     * 에러 응답 (ErrorCode 기반)
     * 사용: ApiResponse.error(errorCode, message)
     *
     * @param errorCode 에러 코드 (ErrorCode Enum)
     * @param message   사용자 정의 에러 메시지 (null일 경우 ErrorCode의 기본 메시지 사용)
     * @param <T>       응답 데이터의 타입
     * @return ApiResponse 실패 응답 객체
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message != null ? message : errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    /**
     * 에러 응답 (문자열 코드 기반)
     * 사용: ApiResponse.error(code, message)
     *
     * @param code    에러 코드 문자열
     * @param message 사용자 정의 에러 메시지 (null일 경우 code 값 사용)
     * @param <T>     응답 데이터의 타입
     * @return ApiResponse 실패 응답 객체
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message != null ? message : code)
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }

}