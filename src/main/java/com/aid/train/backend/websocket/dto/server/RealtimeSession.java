package com.aid.train.backend.websocket.dto.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RealtimeSession (
        String model,
        String instructions,
        String voice,
        
        @JsonProperty("turn_detection")
        TurnDetection turnDetection
) {
    
    /**
     * Turn Detection 설정
     * - null: Server VAD 사용 (기본값, GPT가 자동으로 음성 감지)
     * - type: "server_vad": Server VAD 사용
     * - type: null: Manual 모드 (클라이언트가 직접 제어)
     */
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TurnDetection(
            String type  // null이면 Manual 모드
    ) {}
}
