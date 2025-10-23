package com.aid.train.backend.global.security.logging;

import com.aid.train.backend.global.util.LogMaskingUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 전역 요청/응답 로깅 AOP 클래스입니다.
 * 
 * <p>
 * 모든 {@code @RestController} 계층에서 발생하는 요청과 응답 데이터를 로깅하며,
 * {@link LogMaskingUtil} 을 사용하여 로그 내 민감 정보를 마스킹합니다.
 * 
 * <p>
 * 주요 목적:
 * <ul>
 *   <li>민감 정보(이메일, 토큰, 비밀번호 등)의 로그 노출 방지</li>
 *   <li>로그 포맷 일관성 유지 및 디버깅 지원</li>
 *   <li>개별 컨트롤러 로깅 중복 제거</li>
 * </ul>
 * 
 * <p>
 * {@link org.aspectj.lang.annotation.Around} 어드바이스를 통해 컨트롤러의
 * 요청-응답 과정을 감싸며, 실행 전후에 안전하게 마스킹된 로그를 출력합니다.
 * </p>
 * 
 * @author 왕택준
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class RequestLoggingAspect {

    /**
     * 모든 {@code @RestController} 내 메서드 호출을 감싸며,
     * 요청 파라미터 및 반환 데이터를 마스킹하여 로그에 출력합니다.
     *
     * @param pjp AOP 조인 포인트 (실제 컨트롤러 메서드 실행 시점)
     * @return 컨트롤러 메서드 반환값
     * @throws Throwable 내부 실행 중 예외 발생 시
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logControllerExecution(ProceedingJoinPoint pjp) throws Throwable {
        String controllerName = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();

        // --- 요청 로그 출력 ---
        try {
            log.info("[REQ] {}.{} args={}", controllerName, methodName,
                    LogMaskingUtil.maskSensitiveData(args));
        } catch (Exception e) {
            log.debug("[REQ] {}.{} (args 마스킹 실패: {})", controllerName, methodName, e.getMessage());
        }

        // --- 실제 컨트롤러 실행 ---
        Object result = pjp.proceed();

        // --- 응답 로그 출력 ---
        try {
            log.info("[RES] {}.{} return={}", controllerName, methodName,
                    LogMaskingUtil.maskSensitiveData(result));
        } catch (Exception e) {
            log.debug("[RES] {}.{} (응답 마스킹 실패: {})", controllerName, methodName, e.getMessage());
        }

        return result;
    }
}
