package com.aid.train.backend.global.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * 현재 인증된 사용자 ID를 추출하는 커스텀 어노테이션입니다.
 * JWT에서 사용자 ID를 자동으로 추출하여 파라미터에 주입합니다.
 *
 * <p>
 * 사용 예시:
 * <pre>
 * {@code
 * @GetMapping("/me")
 * public ResponseEntity<?> getMyProfile(@CurrentUserId Long userId) {
 *     // userId는 JWT에서 자동 추출됨
 * }
 * }
 * </pre>
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : #this")
public @interface CurrentUserId {
}