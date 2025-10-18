package com.aid.train.backend.websocket.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GptSession {
    private String sessionId;
    private boolean connected;
    private Object gptRealtimeSession; // 실제 GPT Realtime API 세션 객체

    @Builder.Default
    private List<String> conversationHistory = new ArrayList<>();

    @Builder.Default
    private Queue<byte[]> audioQueue = new LinkedList<>(); // 오디오 데이터 큐

    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
}
