package com.aid.train.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기(@Async) 처리를 위한 스레드 풀 설정 클래스입니다.
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 이메일 발송, 로깅 등 비동기 작업을 처리할 스레드 풀을 생성합니다.
     *
     * @return Executor 빈
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 기본 스레드 수
        executor.setMaxPoolSize(10); // 최대 스레드 수
        executor.setQueueCapacity(25); // 큐 용량
        executor.setThreadNamePrefix("AsyncTask-");
        executor.initialize();
        return executor;
    }
}