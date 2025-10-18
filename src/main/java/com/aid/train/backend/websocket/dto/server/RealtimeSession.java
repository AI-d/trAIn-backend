package com.aid.train.backend.websocket.dto.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
public record RealtimeSession (
        String model,
        String instructions,
        String voice,
        String locale
) {
}
