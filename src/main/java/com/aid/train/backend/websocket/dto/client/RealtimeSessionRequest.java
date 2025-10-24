package com.aid.train.backend.websocket.dto.client;

public record RealtimeSessionRequest (
        String sessionId,
        String model,
        String voice,
        String instructions,
        String sttModel
) {
}
