package com.aid.train.backend.websocket.service;

import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.domain.session.repository.DialogueSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DialogueSession(DB)과 WebSocket 연결을 매핑하는 매니저
 *
 * 역할:
 * - sessionId → DialogueSession(DB) 매핑
 * - WebSocket 연결과 DB 대화 정보 연결
 * - 대화 종료 시 DB 정보 업데이트
 *
 * 왜 필요한가?
 * - WebSocket은 "실시간 연결 정보"만 관리
 * - DB는 "대화 내용, 시나리오, 사용자 정보"를 저장
 * - 이 둘을 연결해야 "누가, 어떤 시나리오로, 무슨 대화를 했는지" 알 수 있음
 *
 * 사용 예시:
 * 1. 사용자 접속 → sessionId로 DB에서 DialogueSession 조회
 * 2. 대화 중 → DB 정보로 시나리오, 프롬프트 확인
 * 3. 대화 종료 → DB에 녹음 파일, transcript 저장
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DialogueSessionMapper {

    private final DialogueSessionRepository dialogueSessionRepository;

    /**
     * sessionId → DialogueSession(DB) 캐시
     *
     * 왜 캐시하나?
     * - DB 조회를 매번 하면 느림
     * - 대화 중에는 같은 세션 정보를 여러 번 조회
     * - 메모리에 캐시해서 빠르게 접근
     */
    private final Map<String, DialogueSession> sessionCache = new ConcurrentHashMap<>();

    /**
     * sessionId로 DialogueSession을 조회합니다.
     *
     * 동작 순서:
     * 1. 캐시에 있으면 캐시에서 반환 (빠름)
     * 2. 캐시에 없으면 DB 조회 후 캐시에 저장
     *
     * 언제 사용하나?
     * - WebSocket 연결 시 시나리오 정보 확인
     * - 대화 중 사용자 정보 확인
     * - GPT 프롬프트 생성 시 시나리오 프롬프트 조회
     *
     * @param sessionId 대화 세션 ID
     * @return DialogueSession (DB 엔티티)
     * @throws IllegalArgumentException 세션을 찾을 수 없을 때
     */
    public DialogueSession getDialogueSession(String sessionId) {
        // 1. 캐시 확인
        DialogueSession cached = sessionCache.get(sessionId);
        if (cached != null) {
            log.debug("캐시에서 DialogueSession 반환 - sessionId: {}", sessionId);
            return cached;
        }

        // 2. DB 조회 (User, Scenario 함께 조회 - N+1 방지)
        DialogueSession session = dialogueSessionRepository
                .findWithUserAndScenarioBySessionId(sessionId)
                .orElseThrow(() -> {
                    log.error("DialogueSession을 찾을 수 없음 - sessionId: {}", sessionId);
                    return new IllegalArgumentException(
                            "세션을 찾을 수 없습니다: " + sessionId);
                });

        // 3. 캐시에 저장
        sessionCache.put(sessionId, session);
        log.info("DialogueSession 조회 및 캐시 저장 - sessionId: {}", sessionId);

        return session;
    }

    /**
     * sessionId로 DialogueSession이 존재하는지 확인합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 존재 여부
     */
    public boolean hasDialogueSession(String sessionId) {
        // 캐시에 있거나 DB에 있으면 true
        return sessionCache.containsKey(sessionId)
                || dialogueSessionRepository.findBySessionId(sessionId).isPresent();
    }

    /**
     * WebSocket 연결 시 DB와 매핑합니다.
     *
     * 언제 호출되나?
     * - AudioHandler의 afterConnectionEstablished()
     * - 클라이언트가 WebSocket 연결 성공했을 때
     *
     * 무엇을 하나?
     * 1. DB에서 DialogueSession 조회
     * 2. 캐시에 저장
     * 3. 유효성 검증 (세션 상태가 ONGOING인지)
     *
     * @param sessionId 대화 세션 ID
     * @throws IllegalArgumentException 세션이 유효하지 않을 때
     */
    public void mapSession(String sessionId) {
        DialogueSession session = getDialogueSession(sessionId);

        // 유효성 검증: ONGOING 상태인지 확인
        if (!session.getStatus().isOngoing()) {
            log.error("유효하지 않은 세션 상태 - sessionId: {}, status: {}",
                    sessionId, session.getStatus());
            throw new IllegalArgumentException(
                    "이미 종료된 세션입니다: " + sessionId);
        }

        log.info("WebSocket ↔ DialogueSession 매핑 완료 - sessionId: {}", sessionId);
    }

    /**
     * 대화 종료 시 매핑을 제거합니다.
     *
     * 언제 호출되나?
     * - SessionCoordinator의 terminateSession()
     * - 사용자가 대화를 종료했을 때
     *
     * 무엇을 하나?
     * - 캐시에서 제거 (메모리 절약)
     * - DB는 그대로 유지 (히스토리로 남김)
     *
     * @param sessionId 대화 세션 ID
     */
    public void unmapSession(String sessionId) {
        DialogueSession removed = sessionCache.remove(sessionId);

        if (removed != null) {
            log.info("DialogueSession 매핑 제거 - sessionId: {}", sessionId);
        } else {
            log.warn("제거할 매핑이 없음 - sessionId: {}", sessionId);
        }
    }

    /**
     * DialogueSession을 업데이트하고 캐시를 갱신합니다.
     *
     * 언제 사용하나?
     * - 대화 종료 시 녹음 파일 URL 저장
     * - 실시간 메트릭 업데이트
     * - Janus/GPT 세션 ID 저장
     *
     * @param sessionId 대화 세션 ID
     * @param session 업데이트된 DialogueSession
     */
    public void updateDialogueSession(String sessionId, DialogueSession session) {
        // DB 저장
        DialogueSession saved = dialogueSessionRepository.save(session);

        // 캐시 갱신
        sessionCache.put(sessionId, saved);

        log.info("DialogueSession 업데이트 완료 - sessionId: {}", sessionId);
    }

    /**
     * 현재 캐시된 세션 수를 반환합니다.
     *
     * @return 캐시된 세션 수
     */
    public int getCachedSessionCount() {
        return sessionCache.size();
    }

    /**
     * 모든 캐시를 제거합니다.
     *
     * 언제 사용하나?
     * - 서버 종료 시
     * - 메모리 정리가 필요할 때
     */
    public void clearCache() {
        int count = sessionCache.size();
        sessionCache.clear();
        log.info("DialogueSession 캐시 전체 제거 - 제거된 세션 수: {}", count);
    }
}