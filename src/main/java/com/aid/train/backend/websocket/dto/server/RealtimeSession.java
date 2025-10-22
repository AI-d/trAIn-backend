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
        Object turnDetection  // null이면 Manual 모드, Map이면 Server VAD 설정
) {}
