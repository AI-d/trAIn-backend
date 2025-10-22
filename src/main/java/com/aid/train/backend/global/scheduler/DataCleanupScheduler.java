package com.aid.train.backend.global.scheduler;

import com.aid.train.backend.domain.user.enums.UserStatus;
import com.aid.train.backend.domain.user.repository.RefreshTokenRepository;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.verification.repository.EmailVerificationRepository;
import com.aid.train.backend.domain.verification.repository.OneTimeCodeRepository;
import com.aid.train.backend.domain.verification.repository.PendingSocialUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 데이터 정리 스케줄러입니다. (리팩토링 버전)
 * <p>
 * 만료된 임시 데이터를 주기적으로 삭제하고, 사용자 계정 정책을 적용하여
 * 데이터베이스의 무결성과 성능을 유지합니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PendingSocialUserRepository pendingSocialUserRepository;
    private final UserRepository userRepository;
    private final OneTimeCodeRepository oneTimeCodeRepository;

    /**
     * 만료되었거나 사용 완료된 임시 데이터를 일괄 정리합니다.
     * <p>
     * 매일 새벽 3시에 실행하여 DB 부하가 적은 시간에 작업을 수행합니다.
     * </p>
     */
    @Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시
    @Transactional
    public void cleanupDailyTemporaryData() {
        log.info("[스케줄러] 일일 임시 데이터 정리 시작");
        try {
            LocalDateTime now = LocalDateTime.now();

            // 1. 만료된 RefreshToken, EmailVerification 토큰 삭제 (벌크 연산)
            int deletedRefreshTokens = refreshTokenRepository.deleteByExpiryDateBefore(now);
            int deletedEmailVerifications = emailVerificationRepository.deleteByExpiryDateBefore(now);

            // 2. 만료되었거나 이미 사용된 PendingSocialUser 정보 삭제 (벌크 연산)
            int deletedPendingUsers = pendingSocialUserRepository.deleteExpiredOrUsedTokens(now);

            int deletedOneTimeCodes = oneTimeCodeRepository.deleteByExpiryDateBefore(now);

            log.info("[스케줄러] 일일 임시 데이터 정리 완료 | RefreshToken: {}건, EmailVerification: {}건, PendingSocialUser: {}건, OneTimeCode: {}건",

                    deletedRefreshTokens, deletedEmailVerifications, deletedPendingUsers, deletedOneTimeCodes);
        } catch (Exception e) {
            log.error("[스케줄러] 일일 임시 데이터 정리 중 오류 발생", e);
        }
    }

    /**
     * 1년 이상 미접속한 사용자를 휴면(INACTIVE) 상태로 전환합니다.
     * <p>
     * 매주 일요일 새벽 4시에 실행됩니다. (주 1회)
     * </p>
     */
    @Scheduled(cron = "0 0 4 * * SUN")  // 매주 일요일 새벽 4시
    @Transactional
    public void convertInactiveUsers() {
        log.info("[스케줄러] 휴면 계정 전환 작업 시작");
        try {
            LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
            int convertedCount = userRepository.convertToInactiveStatus(oneYearAgo);
            if (convertedCount > 0) {
                log.info("[스케줄러] 휴면 계정 전환 완료: {}건", convertedCount);
            } else {
                log.info("[스케줄러] 휴면 계정 전환 대상 없음");
            }
        } catch (Exception e) {
            log.error("[스케줄러] 휴면 계정 전환 중 오류 발생", e);
        }
    }

    /**
     * 탈퇴(WITHDRAWN) 후 30일이 지난 사용자 데이터를 DB에서 완전히 삭제합니다.
     * <p>
     * 매월 1일 새벽 5시에 실행됩니다.
     * </p>
     */
    @Scheduled(cron = "0 0 5 1 * *")  // 매월 1일 새벽 5시
    @Transactional
    public void hardDeleteWithdrawnUsers() {
        log.info("[스케줄러] 탈퇴 사용자 데이터 영구 삭제 작업 시작");
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            // SELECT 후 루프를 도는 대신, 단일 벌크 DELETE 쿼리를 사용하여 성능 최적화
            int deletedCount = userRepository.deleteWithdrawnUsersBefore(UserStatus.WITHDRAWN, thirtyDaysAgo);

            if (deletedCount > 0) {
                log.info("[스케줄러] 탈퇴 사용자 데이터 영구 삭제 완료: {}건", deletedCount);
            } else {
                log.info("[스케줄러] 탈퇴 사용자 영구 삭제 대상 없음");
            }
        } catch (Exception e) {
            log.error("[스케줄러] 탈퇴 사용자 데이터 영구 삭제 중 오류 발생", e);
        }
    }
}