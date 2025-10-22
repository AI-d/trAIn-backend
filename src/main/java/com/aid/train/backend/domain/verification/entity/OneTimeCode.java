package com.aid.train.backend.domain.verification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 소셜 로그인 성공 후 일회용 코드를 관리하는 엔티티입니다.
 * <p>
 * Redis 대신 DB 테이블을 사용하여 일회용 코드를 저장하고 관리합니다.
 * 1분 후 자동 만료되며, 사용 시 즉시 삭제됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Entity
@Table(name = "one_time_codes")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OneTimeCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 일회용 코드 (16자리 UUID 기반)
     * 중복되지 않는 고유한 값이며, 이 값으로 사용자를 식별합니다.
     */
    @Column(unique = true, nullable = false, length = 16)
    private String code;

    /**
     * 코드와 연결된 사용자 ID
     * User 엔티티와 직접 연관관계를 맺지 않고 문자열로 저장합니다.
     */
    @Column(nullable = false)
    private String userId;

    /**
     * 코드 만료 시간 (생성 시점 + 1분)
     * 이 시간이 지나면 코드는 더 이상 사용할 수 없습니다.
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    /**
     * 코드 사용 여부
     * 코드가 한 번 사용되면 true로 설정되고, 즉시 삭제됩니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    /**
     * 코드가 만료되었는지 확인합니다.
     *
     * @return 현재 시간이 만료 시간을 넘었으면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    /**
     * 코드를 사용됨으로 표시합니다.
     * 실제로는 사용 즉시 삭제되므로 이 메서드는 로깅용으로만 사용됩니다.
     */
    public void markAsUsed() {
        this.used = true;
    }
}