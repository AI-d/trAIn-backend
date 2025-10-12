package com.aid.train.backend.global.scheduler;

import com.aid.train.backend.domain.user.enums.UserStatus;
import com.aid.train.backend.domain.user.repository.RefreshTokenRepository;
import com.aid.train.backend.domain.user.repository.UserRepository;
import com.aid.train.backend.domain.verification.repository.EmailVerificationRepository;
import com.aid.train.backend.domain.verification.repository.PendingSocialUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 데이터 정리 스케줄러입니다.
 * 만료된 데이터를 주기적으로 삭제하여 데이터베이스 성능을 유지합니다.
 *
 * <p>
 * 주요 정리 작업:
 * <ul>
 *   <li>만료된 RefreshToken 삭제 (만료 후 14일)</li>
 *   <li>만료된 EmailVerification 삭제 (생성 후 1시간)</li>
 *   <li>만료된 PendingSocialUser 삭제 (생성 후 1시간)</li>
 *   <li>휴면 계정 전환 (1년 미접속)</li>
 * </ul>
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

    /**
     * 만료된 RefreshToken을 삭제합니다.
     * 매일 새벽 3시에 실행되며, 만료 후 14일이 지난 토큰을 삭제합니다.
     */
    @Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        log.info("[스케줄러] 만료된 RefreshToken 정리 시작");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(14);
        int deletedCount = refreshTokenRepository.bulkDeleteExpiredTokens(cutoffDate);

        log.info("[스케줄러] 만료된 RefreshToken 정리 완료 - 삭제 건수: {}", deletedCount);
    }

    /**
     * 폐기되었거나 만료된 RefreshToken을 삭제합니다.
     * 매일 새벽 4시에 실행됩니다.
     */
    @Scheduled(cron = "0 0 4 * * *")  // 매일 새벽 4시
    @Transactional
    public void cleanupInvalidRefreshTokens() {
        log.info("[스케줄러] 폐기/만료 RefreshToken 정리 시작");

        LocalDateTime now = LocalDateTime.now();
        int deletedCount = refreshTokenRepository.bulkDeleteInvalidTokens(now);

        log.info("[스케줄러] 폐기/만료 RefreshToken 정리 완료 - 삭제 건수: {}", deletedCount);
    }

    /**
     * 만료된 이메일 인증 정보를 삭제합니다.
     * 매 시간 정각에 실행되며, 생성 후 1시간이 지난 데이터를 삭제합니다.
     */
    @Scheduled(cron = "0 0 * * * *")  // 매 시간 정각
    @Transactional
    public void cleanupExpiredEmailVerifications() {
        log.info("[스케줄러] 만료된 EmailVerification 정리 시작");

        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(1);
        int deletedCount = emailVerificationRepository.bulkDeleteOldVerifications(cutoffDate);

        log.info("[스케줄러] 만료된 EmailVerification 정리 완료 - 삭제 건수: {}", deletedCount);
    }

    /**
     * 만료된 임시 소셜 사용자 정보를 삭제합니다.
     * 매 시간 정각에 실행되며, 생성 후 1시간이 지난 데이터를 삭제합니다.
     */
    @Scheduled(cron = "0 0 * * * *")  // 매 시간 정각
    @Transactional
    public void cleanupExpiredPendingSocialUsers() {
        log.info("[스케줄러] 만료된 PendingSocialUser 정리 시작");

        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(1);
        int deletedCount = pendingSocialUserRepository.bulkDeleteOldPendingUsers(cutoffDate);

        log.info("[스케줄러] 만료된 PendingSocialUser 정리 완료 - 삭제 건수: {}", deletedCount);
    }

    /**
     * 완료된 임시 소셜 사용자 정보를 삭제합니다.
     * 매일 새벽 2시에 실행됩니다.
     */
    @Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
    @Transactional
    public void cleanupCompletedPendingSocialUsers() {
        log.info("[스케줄러] 완료된 PendingSocialUser 정리 시작");

        int deletedCount = pendingSocialUserRepository.bulkDeleteCompletedUsers();

        log.info("[스케줄러] 완료된 PendingSocialUser 정리 완료 - 삭제 건수: {}", deletedCount);
    }

    /**
     * 1년 이상 미접속 사용자를 휴면 상태로 전환합니다.
     * 매일 새벽 1시에 실행됩니다.
     *
     * <p>개인정보보호법 제39조의6에 따라 1년 이상 서비스를 이용하지 않은
     * 사용자의 개인정보를 분리 보관합니다.</p>
     */
    @Scheduled(cron = "0 0 1 * * *")  // 매일 새벽 1시
    @Transactional
    public void convertInactiveUsers() {
        log.info("[스케줄러] 휴면 계정 전환 시작");

        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        var users = userRepository.findByStatusAndUpdatedAtBefore(UserStatus.ACTIVE, oneYearAgo);

        int convertedCount = 0;
        for (var user : users) {
            user.convertToInactive();
            convertedCount++;
        }

        log.info("[스케줄러] 휴면 계정 전환 완료 - 전환 건수: {}", convertedCount);
    }

    /**
     * 탈퇴 후 30일이 지난 사용자 데이터를 완전히 삭제합니다.
     * 매일 새벽 5시에 실행됩니다.
     *
     * <p>개인정보 보관 기간 정책에 따라 탈퇴 후 30일이 지난
     * 사용자의 데이터를 완전히 삭제합니다.</p>
     */
    @Scheduled(cron = "0 0 5 * * *")  // 매일 새벽 5시
    @Transactional
    public void deleteWithdrawnUsers() {
        log.info("[스케줄러] 탈퇴 사용자 데이터 완전 삭제 시작");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        var users = userRepository.findByStatusAndDeletedAtBefore(UserStatus.WITHDRAWN, thirtyDaysAgo);

        int deletedCount = 0;
        for (var user : users) {
            userRepository.delete(user);
            deletedCount++;
        }

        log.info("[스케줄러] 탈퇴 사용자 데이터 완전 삭제 완료 - 삭제 건수: {}", deletedCount);
    }
}