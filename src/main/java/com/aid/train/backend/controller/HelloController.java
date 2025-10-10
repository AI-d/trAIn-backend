package com.aid.train.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Hello World 및 Health Check용 컨트롤러
 * 임시: 2025.10.10 배포 테스트용, 추후 삭제 예정
 *
 * @author 김경민
 * @date 2025.10.10
 */
@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello World from Spring Boot!";
    }

    @GetMapping("/api/health")
    public String health() {
        return "OK - dialogym backend is running!";
    }
}