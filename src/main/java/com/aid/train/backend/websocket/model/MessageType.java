package com.aid.train.backend.websocket.model;

/**
 * WebSocket 메시지 타입
 *
 * 용도별 분류:
 * - Signaling: WebRTC 연결 설정
 * - Audio: 음성 데이터 송수신
 * - Feedback: 실시간 분석 결과
 * - Control: 연결 관리
 *
 * @author 김경민
 * @since 2025-10-13
 * @version 1.0.0
 */
public enum MessageType {
    // Signaling 메시지
    OFFER,           // SDP Offer
    ANSWER,          // SDP Answer
    ICE_CANDIDATE,   // ICE Candidate

    // ===== 음성 데이터 =====
    AUDIO_DATA,      // 사용자 음성 데이터 (바이너리)
    AI_AUDIO,        // AI 음성 응답 (바이너리)

    // ===== AI 응답 =====
    AI_RESPONSE,     // AI 텍스트 응답 (대화 내용)
    AI_TRANSCRIPT,   // AI가 인식한 사용자 발화 (STT 결과)

    // Feedback 메시지
    FEEDBACK,        // 종합 피드백 (발화 속도, 추임새, 음량 등)
    SPEED,           // 발화 속도
    FILLER,          // 추임새
    EMOTION,         // 감정 분석

    // 제어 메시지
    ERROR,           // 에러
    PING,            // 연결 유지 (Heartbeat)
    PONG             // Ping 응답
}
