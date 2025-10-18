package com.aid.train.backend.domain.session.controller;

import com.aid.train.backend.domain.session.dto.request.CreateSessionRequest;
import com.aid.train.backend.domain.session.dto.response.SessionResponse;
import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.domain.session.service.DialogueSessionService;
import com.aid.train.backend.global.exception.TrainException;
import com.aid.train.backend.global.response.ApiResponse;
import com.aid.train.backend.global.exception.enums.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 대화 세션 관리 컨트롤러
 *
 * 역할:
 * - 새로운 대화 세션 생성
 * - 세션 상태 조회
 * - 세션 종료 처리
 *
 * @author 김경민
 * @since 2025-10-17
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class DialogueSessionController {

    private final DialogueSessionService dialogueSessionService;

    /**
     * 새로운 대화 세션을 생성합니다.
     *
     * 프론트엔드 흐름:
     * 1. 사용자가 "대화 시작" 버튼 클릭
     * 2. POST /api/sessions 호출 (userId, scenarioId 전송)
     * 3. 서버가 sessionId 반환
     * 4. 프론트가 받은 sessionId로 WebSocket 연결
     *
     * @param request 세션 생성 요청 (userId, scenarioId)
     * @return 생성된 세션 정보 (sessionId 포함)
     */
    @PostMapping
    public ResponseEntity<?> createSession(
            @Valid @RequestBody CreateSessionRequest request
    ) {
        log.info("세션 생성 요청 - userId: {}, scenarioId: {}",
                request.userId(), request.scenarioId());

        try {
            // DialogueSession 생성 및 DB 저장
            DialogueSession session = dialogueSessionService.createSession(
                    request.userId(),
                    request.scenarioId()
            );

            // 생성 직후 재조회로 확실하게 DB 반영 확인
            DialogueSession verifiedSession = dialogueSessionService.getSession(
                    session.getSessionId()
            );

            // 응답 DTO 생성
            SessionResponse response = SessionResponse.builder()
                    .sessionId(session.getSessionId())
                    .userId(session.getUser().getId())
                    .scenarioId(session.getScenario().getId())
                    .scenarioTitle(session.getScenario().getTitle())
                    .status(session.getStatus().name())
                    .startedAt(session.getStartedAt())
                    .build();

            log.info("세션 생성 완료 - sessionId: {}", session.getSessionId());

            return ResponseEntity.ok(
                    ApiResponse.success("대화 세션이 생성되었습니다.", response)
            );

        } catch (TrainException e) {
            log.error("세션 생성 실패 (TrainException) - userId: {}, scenarioId: {}, errorCode: {}",
                    request.userId(), request.scenarioId(), e.getErrorCode(), e);

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode())
            );

        } catch (Exception e) {
            log.error("세션 생성 실패 (Exception) - userId: {}, scenarioId: {}",
                    request.userId(), request.scenarioId(), e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "세션 생성 중 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 세션 상태를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 세션 정보
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable String sessionId) {
        log.info("세션 조회 요청 - sessionId: {}", sessionId);

        try {
            DialogueSession session = dialogueSessionService.getSessionWithUserAndScenario(sessionId);

            SessionResponse response = SessionResponse.builder()
                    .sessionId(session.getSessionId())
                    .userId(session.getUser().getId())
                    .scenarioId(session.getScenario().getId())
                    .scenarioTitle(session.getScenario().getTitle())
                    .status(session.getStatus().name())
                    .startedAt(session.getStartedAt())
                    .endedAt(session.getEndedAt())
                    .audioDurationSeconds(session.getAudioDurationSeconds())
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success("세션 조회 성공", response)
            );

        } catch (TrainException e) {
            log.error("세션 조회 실패 (TrainException) - sessionId: {}, errorCode: {}",
                    sessionId, e.getErrorCode(), e);

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode())
            );

        } catch (Exception e) {
            log.error("세션 조회 실패 (Exception) - sessionId: {}", sessionId, e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "세션 조회 중 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 세션을 정상 종료합니다.
     *
     * @param sessionId 세션 ID
     * @return 종료 결과
     */
    @PutMapping("/{sessionId}/complete")
    public ResponseEntity<?> completeSession(@PathVariable String sessionId) {
        log.info("세션 종료 요청 - sessionId: {}", sessionId);

        try {
            dialogueSessionService.completeSession(sessionId);

            return ResponseEntity.ok(
                    ApiResponse.success("세션이 정상 종료되었습니다.", null)
            );

        } catch (TrainException e) {
            log.error("세션 종료 실패 (TrainException) - sessionId: {}, errorCode: {}",
                    sessionId, e.getErrorCode(), e);

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode())
            );

        } catch (Exception e) {
            log.error("세션 종료 실패 (Exception) - sessionId: {}", sessionId, e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "세션 종료 중 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 세션을 실패 처리합니다.
     *
     * @param sessionId 세션 ID
     * @return 실패 처리 결과
     */
    @PutMapping("/{sessionId}/fail")
    public ResponseEntity<?> failSession(@PathVariable String sessionId) {
        log.info("세션 실패 처리 요청 - sessionId: {}", sessionId);

        try {
            dialogueSessionService.failSession(sessionId);

            return ResponseEntity.ok(
                    ApiResponse.success("세션이 실패 처리되었습니다.", null)
            );

        } catch (TrainException e) {
            log.error("세션 실패 처리 실패 (TrainException) - sessionId: {}, errorCode: {}",
                    sessionId, e.getErrorCode(), e);

            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode())
            );

        } catch (Exception e) {
            log.error("세션 실패 처리 실패 (Exception) - sessionId: {}", sessionId, e);

            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "세션 실패 처리 중 오류가 발생했습니다.")
            );
        }
    }
}