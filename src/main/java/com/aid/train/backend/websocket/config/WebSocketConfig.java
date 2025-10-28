package com.aid.train.backend.websocket.config;

import com.aid.train.backend.websocket.handler.FeedbackHandler;
import com.aid.train.backend.websocket.handler.SignalingHandler;
import com.aid.train.backend.websocket.handler.TranscriptHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket 연결을 설정하기 위한 설정 클래스
 *
 * 역할:
 * - 3개의 WebSocket 엔드포인트 등록
 * - CORS 정책 설정
 * - 각 핸들러와 URL 매핑
 * - WebSocket 메시지 버퍼 크기 설정
 *
 * 엔드포인트:
 * 1. /ws/audio/{sessionId} - 음성 데이터 송수신 (AudioHandler)
 * 2. /ws/signaling/{sessionId} - WebRTC 시그널링 (SignalingHandler)
 * 3. /ws/feedback/{sessionId} - 실시간 피드백 전송 (FeedbackHandler)
 *
 * @author 김경민
 * @since 2025-10-15
 * @version 1.3.0
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    // private final AudioHandler audioHandler;
    private final SignalingHandler signalingHandler;
    private final FeedbackHandler feedbackHandler;
    private final TranscriptHandler transcriptHandler;

    /**
     * WebSocket 핸들러를 등록합니다.
     *
     * 각 엔드포인트의 역할:
     * - /ws/audio: 사용자 음성 → 서버 → GPT → AI 음성 → 사용자 (양방향)
     * - /ws/signaling: WebRTC Offer/Answer/ICE Candidate 교환
     * - /ws/feedback: 서버 → 사용자 (단방향, 실시간 분석 결과)
     *
     * @param registry WebSocket 핸들러를 등록하기 위한 레지스트리
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // 1. Audio WebSocket 엔드포인트 - 음성 데이터 송수신
        /*registry.addHandler(audioHandler, "/ws/audio/{sessionId}")
                .setAllowedOriginPatterns(
                        "*"  // 개발 환경: 모든 도메인 허용
                        // 프로덕션 환경에서는 아래 주석 해제하고 "*" 제거
                        // "https://dialogym.shop",
                        // "https://www.dialogym.shop"
                );*/

        // 2. Signaling WebSocket 엔드포인트 - WebRTC 연결 설정
        /*registry.addHandler(signalingHandler, "/ws/signaling/{sessionId}")
                .setAllowedOriginPatterns(
                        "*"  // 개발 환경: 모든 도메인 허용
                        // 프로덕션 환경에서는 아래 주석 해제하고 "*" 제거
                        // "https://dialogym.shop",
                        // "https://www.dialogym.shop"
                );*/

        // transcript 및 세션 연결 WebSocket 엔드포인트
        registry.addHandler(transcriptHandler, "/ws/transcript/{sessionId}")
                .setAllowedOriginPatterns(
                        "*"  // 개발 환경: 모든 도메인 허용
                        // 프로덕션 환경에서는 아래 주석 해제하고 "*" 제거
                        // "https://dialogym.shop",
                        // "https://www.dialogym.shop"
                );

        // 3. Feedback WebSocket 엔드포인트 - 실시간 피드백 전송
        registry.addHandler(feedbackHandler, "/ws/feedback/{sessionId}")
                .setAllowedOriginPatterns(
                        "*"  // 개발 환경: 모든 도메인 허용
                        // 프로덕션 환경에서는 아래 주석 해제하고 "*" 제거
                        // "https://dialogym.shop",
                        // "https://www.dialogym.shop"
                );
    }

    /**
     * WebSocket 컨테이너 설정
     * 
     * GPT Realtime API의 큰 오디오 델타 메시지를 처리하기 위해
     * 메시지 버퍼 크기를 증가시킵니다.
     * 
     * @return ServletServerContainerFactoryBean
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        
        // 텍스트 메시지 버퍼 크기: 1MB (GPT 오디오 델타 처리용)
        container.setMaxTextMessageBufferSize(1024 * 1024);
        
        // 바이너리 메시지 버퍼 크기: 1MB
        container.setMaxBinaryMessageBufferSize(1024 * 1024);
        
        // 세션 타임아웃: 15분
        container.setMaxSessionIdleTimeout(15 * 60000L);
        
        return container;
    }
}