package com.aid.train.backend.websocket.controller;

import com.aid.train.backend.websocket.dto.server.FeedbackMessage;
import com.aid.train.backend.websocket.handler.FeedbackHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final FeedbackHandler feedbackHandler;

    @GetMapping("/test/feedback/{sessionId}")
    public String sendTestFeedback(@PathVariable String sessionId) {
        FeedbackMessage mockFeedback = FeedbackMessage.builder()
                .sessionId(sessionId)
                .wpm(120)
                .tip("발화 속도가 안정적입니다!")
                .build();

        feedbackHandler.sendFeedback(sessionId, mockFeedback);

        return sessionId + "에게 테스트 피드백 전송 완료";
    }
}
