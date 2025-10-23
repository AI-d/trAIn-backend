package com.aid.train.backend.domain.session.service;

import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.domain.session.entity.Transcript;
import com.aid.train.backend.domain.session.enums.Speaker;
import com.aid.train.backend.domain.session.repository.TranscriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transcript 저장 및 조회 서비스
 *
 * 역할:
 * - GPT Realtime API로부터 받은 STT 결과를 DB에 저장
 * - 사용자/AI 발화 내역 관리
 *
 * @author 김경민
 * @since 2025-10-23
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TranscriptService {

    private final TranscriptRepository transcriptRepository;
    private final DialogueSessionService dialogueSessionService;

    /**
     * 사용자 발화를 저장합니다.
     *
     * @param sessionId 세션 ID
     * @param content 발화 내용
     */
    public void saveUserTranscript(String sessionId, String content) {
        saveTranscript(sessionId, Speaker.USER, content);
    }

    /**
     * AI 발화를 저장합니다.
     *
     * @param sessionId 세션 ID
     * @param content 발화 내용
     */
    public void saveAiTranscript(String sessionId, String content) {
        saveTranscript(sessionId, Speaker.AI, content);
    }

    /**
     * 발화 내역을 DB에 저장합니다.
     *
     * @param sessionId 세션 ID
     * @param speaker 발화자 (USER 또는 AI)
     * @param content 발화 내용
     */
    private void saveTranscript(String sessionId, Speaker speaker, String content) {
        try {
            log.info("발화 저장 시작 - sessionId: {}, speaker: {}, content: {}",
                    sessionId, speaker, content);

            // 1. DialogueSession 조회
            DialogueSession dialogueSession = dialogueSessionService.getSession(sessionId);

            // 2. Transcript 엔티티 생성
            Transcript transcript = Transcript.builder()
                    .dialogueSession(dialogueSession)
                    .speaker(speaker)
                    .content(content)
                    .timestamp(LocalDateTime.now())
                    .build();

            // 3. DB 저장
            transcriptRepository.save(transcript);

            log.info("발화 저장 완료 - sessionId: {}, speaker: {}", sessionId, speaker);

        } catch (Exception e) {
            log.error("발화 저장 실패 - sessionId: {}, speaker: {}", sessionId, speaker, e);
            throw new RuntimeException("발화 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 세션의 모든 발화 내역을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 발화 내역 목록
     */
    @Transactional(readOnly = true)
    public List<Transcript> getTranscripts(String sessionId) {
        DialogueSession dialogueSession = dialogueSessionService.getSession(sessionId);
        return transcriptRepository.findByDialogueSessionIdOrderByTimestampAsc(
                dialogueSession.getId());
    }

    /**
     * 특정 세션의 사용자 발화만 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 사용자 발화 목록
     */
    @Transactional(readOnly = true)
    public List<Transcript> getUserTranscripts(String sessionId) {
        DialogueSession dialogueSession = dialogueSessionService.getSession(sessionId);
        return transcriptRepository.findByDialogueSessionIdAndSpeakerOrderByTimestampAsc(
                dialogueSession.getId(), Speaker.USER);
    }

    /**
     * 특정 세션의 AI 발화만 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return AI 발화 목록
     */
    @Transactional(readOnly = true)
    public List<Transcript> getAiTranscripts(String sessionId) {
        DialogueSession dialogueSession = dialogueSessionService.getSession(sessionId);
        return transcriptRepository.findByDialogueSessionIdAndSpeakerOrderByTimestampAsc(
                dialogueSession.getId(), Speaker.AI);
    }
}
