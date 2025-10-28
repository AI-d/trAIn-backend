package com.aid.train.backend.websocket.dto.server;

import lombok.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GptSession {

    private String sessionId;
    private final List<Map<String, Object>> iceCandidates = new ArrayList<>();

    @Builder.Default
    private AtomicBoolean isReady = new AtomicBoolean(false);

    // 실제 GPT Realtime API 세션 객체
    private Object gptRealtimeSession;

    private WebSocketSession webSocketSession;

    @Builder.Default
    private List<String> conversationHistory = new ArrayList<>();

    /*@Builder.Default
    private Queue<byte[]> audioQueue = new LinkedList<>();*/

    // ICE Candidate 추가 메서드
    public void addIceCandidate(Map<String, Object> candidate) {
        if (candidate != null) {
            this.iceCandidates.add(candidate);
        }
    }

    public void setReady(boolean ready) {
        this.isReady.set(ready);
    }

}
