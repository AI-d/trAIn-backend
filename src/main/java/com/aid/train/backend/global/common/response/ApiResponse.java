package com.aid.train.backend.global.common.response;

/**
 * 클라이언트에게 일관적인 응답 포맷을 제공하기 위한 객체입니다.
 *
 * <p>사용 예시:</p>
 * <pre>
 * // Controller에서의 사용
 * @GetMapping("/users/{id}")
 * public ResponseEntity<> getUser(@PathVariable Long id) {
 *     UserDto user = userService.getUserById(id);
 *     return ResponseEntity.ok()
 *                          .body(ApiResponse.success("사용자 조회 성공", user));
 * }
 * </pre>
 */
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
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

    /**
     * Api 성공 응답을 생성합니다.
     *
     * @param message API 응답 메시지
     * @param <T> 응답 데이터의 타입
     * @param data 응답 데이터 (모든 타입 가능)
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
}
