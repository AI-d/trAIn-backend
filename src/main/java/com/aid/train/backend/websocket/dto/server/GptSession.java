package com.aid.train.backend.websocket.dto.server;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GptSession {

    private String sessionId;
    private boolean isReady = false;

    private Object gptRealtimeSession;// 실제 GPT Realtime API 세션 객체

    @Builder.Default
    private List<String> conversationHistory = new ArrayList<>();

    @Builder.Default
    private Queue<byte[]> audioQueue = new LinkedList<>();

}
