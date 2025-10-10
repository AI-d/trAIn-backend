package com.aid.train.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing  //@CreatedDate, @LastModifiedDate 작동을 위해 필요
public class TrAInBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrAInBackendApplication.class, args);
    }

}
