package com.aid.train.backend.websocket.dto.server;

import lombok.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GptSession {

    private String sessionId;

    @Builder.Default
    private AtomicBoolean isReady = new AtomicBoolean(false);

    // 실제 GPT Realtime API 세션 객체
    private Object gptRealtimeSession;

    private WebSocketSession webSocketSession;

    @Builder.Default
    private List<String> conversationHistory = new ArrayList<>();

    @Builder.Default
    private Queue<byte[]> audioQueue = new LinkedList<>();

    public void setReady(boolean ready) {
        this.isReady.set(ready);
    }

}
