package com.aid.train.backend.domain.session.controller;

import com.aid.train.backend.domain.session.dto.response.TranscriptResponse;
import com.aid.train.backend.domain.session.entity.Transcript;
import com.aid.train.backend.domain.session.service.TranscriptService;
import com.aid.train.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transcript 조회 API
 *
 * @author 김경민
 * @since 2025-10-23
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transcripts")
@RequiredArgsConstructor
@Tag(name = "Transcript", description = "발화 내역 API")
public class TranscriptController {

    private final TranscriptService transcriptService;

    @Operation(summary = "세션의 모든 발화 내역 조회", description = "특정 세션의 모든 발화 내역을 시간순으로 조회합니다.")
    @GetMapping("/{sessionId}")
    public ApiResponse<List<TranscriptResponse>> getTranscripts(
            @PathVariable String sessionId) {
        
        log.info("발화 내역 조회 요청 - sessionId: {}", sessionId);
        
        List<Transcript> transcripts = transcriptService.getTranscripts(sessionId);
        List<TranscriptResponse> responses = transcripts.stream()
                .map(TranscriptResponse::from)
                .collect(Collectors.toList());
        
        log.info("발화 내역 조회 완료 - sessionId: {}, count: {}", sessionId, responses.size());
        
        return ApiResponse.ok(responses);
    }

    @Operation(summary = "사용자 발화만 조회", description = "특정 세션의 사용자 발화만 조회합니다.")
    @GetMapping("/{sessionId}/user")
    public ApiResponse<List<TranscriptResponse>> getUserTranscripts(
            @PathVariable String sessionId) {
        
        log.info("사용자 발화 조회 요청 - sessionId: {}", sessionId);
        
        List<Transcript> transcripts = transcriptService.getUserTranscripts(sessionId);
        List<TranscriptResponse> responses = transcripts.stream()
                .map(TranscriptResponse::from)
                .collect(Collectors.toList());
        
        log.info("사용자 발화 조회 완료 - sessionId: {}, count: {}", sessionId, responses.size());
        
        return ApiResponse.ok(responses);
    }

    @Operation(summary = "AI 발화만 조회", description = "특정 세션의 AI 발화만 조회합니다.")
    @GetMapping("/{sessionId}/ai")
    public ApiResponse<List<TranscriptResponse>> getAiTranscripts(
            @PathVariable String sessionId) {
        
        log.info("AI 발화 조회 요청 - sessionId: {}", sessionId);
        
        List<Transcript> transcripts = transcriptService.getAiTranscripts(sessionId);
        List<TranscriptResponse> responses = transcripts.stream()
                .map(TranscriptResponse::from)
                .collect(Collectors.toList());
        
        log.info("AI 발화 조회 완료 - sessionId: {}, count: {}", sessionId, responses.size());
        
        return ApiResponse.ok(responses);
    }
}
