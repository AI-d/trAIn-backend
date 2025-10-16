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

    // Client → Server
    SESSION_INIT,    // 세션 초기 파라미터 전달
    AUDIO_DATA,      // 오디오 프레임 업스트림
    SESSION_CLOSE,   // 정상 종료 의사 전달

    // Server → Client
    AI_TRANSCRIPT,   // 사용자 발화 STT 결과
    AI_RESPONSE,     // AI 텍스트 응답
    AI_AUDIO,        // AI 오디오 응답(TTS 등)

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
