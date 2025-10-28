package com.aid.train.backend.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 설정 클래스
 *
 * OpenAI ChatGPT 모델과 연동하기 위한 ChatClient Bean을 제공합니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Configuration
public class ChatClientConfig {

    /**
     * ChatClient Bean을 생성합니다.
     *
     * @param chatModel OpenAI ChatModel
     * @return 설정된 ChatClient 인스턴스
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}