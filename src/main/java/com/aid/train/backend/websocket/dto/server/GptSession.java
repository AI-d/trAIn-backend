package com.aid.train.backend.websocket.dto.server;

import lombok.*;

import java.time.LocalDateTime;
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
    private AtomicBoolean isReady = new AtomicBoolean(false);

    private Object gptRealtimeSession;// 실제 GPT Realtime API 세션 객체

    @Builder.Default
    private List<String> conversationHistory = new ArrayList<>();

    @Builder.Default
    private Queue<byte[]> audioQueue = new LinkedList<>();

    public void setReady(boolean ready) {
        this.isReady.set(ready);
    }

}
