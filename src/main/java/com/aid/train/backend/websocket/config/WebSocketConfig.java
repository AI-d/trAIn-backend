package com.aid.train.backend.websocket.config;

import com.aid.train.backend.websocket.handler.FeedbackHandler;
import com.aid.train.backend.websocket.handler.SignalingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 연결을 설정하기 위한 설정 클래스
 * WebSocketConfigurer 인터페이스를 구현하여 WebSocket 핸들러를 등록합니다.
 *
 * @author 김경민
 * @since 2025-10-13
 * @version 1.1.0
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SignalingHandler signalingHandler;
//    private final FeedbackHandler feedbackHandler;

    /**
     * WebSocket 핸들러를 등록합니다.
     * 특정 URI 패턴에 대해 WebSocket 연결 엔드포인트를 설정하고, 연결 허용 조건을 구성합니다.
     *
     * @param registry WebSocket 핸들러를 등록하기 위한 WebSocketHandlerRegistry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Signaling WebSocket 엔드포인트 (WebRTC용 - 음성 데이터 송수신)
        registry.addHandler(signalingHandler, "/ws/signaling/{sessionId}")
                .setAllowedOriginPatterns(
                        "*"
//                        ,"https://dialogym.shop"
//                        ,"https://www.dialogym.shop"
                        ); // 개발 중에는 * 모든 도메인 허용, 배포 시 주석 해제하고 "*" 제거

        // Feedback WebSocket 엔드포인트 (실시간 분석 결과 전송)
        /*registry.addHandler(feedbackHandler, "/ws/feedback/{sessionId}")
                .setAllowedOriginPatterns(
                        "*"
//                        ,"https://dialogym.shop"
//                        ,"https://www.dialogym.shop"
                ); // 개발 중에는 *, 배포 시 특정 도메인으로 제한*/

    }
}
