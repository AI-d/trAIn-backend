package com.aid.train.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync // EmailService를 위해 이전에 추가했던 어노테이션
@EnableScheduling // 스케줄러를 활성화하기 위해 이 어노테이션을 추가합니다.
public class TrAInBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrAInBackendApplication.class, args);
    }

}
