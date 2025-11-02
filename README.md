# trAIn Backend

> AI 기반 대화 훈련 플랫폼 Dialogym의 백엔드 애플리케이션

Aid 팀의 trAIn 프로젝트 중 Dialogym 서비스의 백엔드입니다. 사용자가 AI와 실시간 대화를 통해 커뮤니케이션 스킬을 향상시킬 수 있는 플랫폼의 서버 시스템을 제공합니다.

## 시작하기

### 필수 요구사항

- Java 17 이상
- MariaDB 10.x
- Gradle 8.x

### 설치 및 실행

```bash
# 저장소 클론
git clone https://github.com/AI-d/trAIn-backend.git
cd trAIn-backend

# 환경 변수 설정
copy .env.template .env
# .env 파일을 열어 필요한 값 설정

# 데이터베이스 생성
# MariaDB에 접속하여 실행:
# CREATE DATABASE dialogym CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 빌드 및 실행 (http://localhost:9090)
./gradlew bootRun
```

### 환경 변수

`.env` 파일에 다음 값을 설정하세요:

```env
# 데이터베이스
DB_HOST=localhost
DB_PORT=3306
DB_NAME=dialogym
DB_USER=root
DB_PASS=your_database_password

# JWT (openssl rand -base64 64로 생성)
JWT_SECRET=your-jwt-secret-key-minimum-256-bits

# OpenAI API
OPENAI_API_KEY=sk-proj-your-openai-api-key

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5050
FRONTEND_BASE_URL=http://localhost:5050
```

## 디렉토리 구조

```
src/main/java/com/aid/train/backend/
├── domain/                    # 도메인 계층
│   ├── user/                  # 사용자 관리
│   │   ├── controller/       # REST API 컨트롤러
│   │   ├── service/          # 비즈니스 로직
│   │   ├── repository/       # 데이터 접근
│   │   ├── entity/           # JPA 엔티티
│   │   └── dto/              # 데이터 전송 객체
│   ├── verification/          # 이메일 인증
│   ├── terms/                 # 약관 관리
│   ├── scenario/              # 시나리오 관리
│   ├── session/               # 대화 세션
│   └── feedback/              # AI 피드백
│
├── websocket/                 # WebSocket 통신
│   ├── controller/           # WebSocket 엔드포인트
│   ├── handler/              # 메시지 핸들러
│   └── service/              # WebSocket 서비스
│
└── global/                    # 공통 계층
    ├── config/                # 설정 (Security, CORS, Cache 등)
    ├── security/              # 보안 (JWT, Filter, Handler)
    ├── exception/             # 예외 처리
    ├── response/              # 공통 응답 포맷
    ├── util/                  # 유틸리티
    └── scheduler/             # 스케줄러
```

## 주요 기능

### 인증
- 이메일/비밀번호 회원가입 및 로그인
- 소셜 로그인 (Google, Kakao, Naver)
- 이메일 인증
- JWT 토큰 관리 (Access Token + Refresh Token)
- HttpOnly 쿠키 기반 토큰 저장

### 시나리오
- 기본 시나리오 제공
- 커스텀 시나리오 생성
- 시나리오 검색 및 필터링

### 대화 세션
- 대화 세션 생성 및 관리
- 대화 히스토리 저장
- 세션 상태 추적

### AI 피드백
- OpenAI GPT 기반 대화 분석
- 문법, 어휘, 유창성 평가
- 개선 제안 및 대안 표현 제공
- 피드백 히스토리 관리

### 실시간 통신
- WebSocket 기반 실시간 메시징
- OpenAI Realtime API 연동
- Ephemeral Key 발급

## 기술 스택

### Core
- Java 17 - 프로그래밍 언어
- Spring Boot 3.5.5 - 애플리케이션 프레임워크
- Gradle 8.x - 빌드 도구

### 데이터베이스
- MariaDB 10.x - 관계형 데이터베이스
- Spring Data JPA - ORM
- Hibernate - JPA 구현체
- QueryDSL 5.0.0 - 타입 안전 쿼리
- p6spy 1.9.1 - 쿼리 로깅

### 보안
- Spring Security - 인증/인가 프레임워크
- JWT (jjwt 0.12.3) - 토큰 기반 인증
- OAuth2 Client - 소셜 로그인
- Bucket4j 8.10.1 - Rate Limiting

### AI & 통신
- Spring AI 1.0.0-M4 - AI 통합 프레임워크
- OpenAI API - GPT 모델 및 Realtime API
- WebSocket - 실시간 양방향 통신
- Spring Mail - 이메일 발송

### 캐싱 & 성능
- Spring Cache - 캐시 추상화
- Caffeine 3.1.8 - 고성능 인메모리 캐시

### 문서화 & 모니터링
- SpringDoc OpenAPI 2.8.13 - API 문서 자동 생성 (Swagger UI)
- Spring Boot Actuator - 애플리케이션 모니터링

### 개발 도구
- Lombok - 보일러플레이트 코드 제거
- Spring Dotenv 4.0.0 - 환경 변수 관리
- Spring Boot DevTools - 개발 편의 기능

## API 문서

서버 실행 후 Swagger UI에서 전체 API 문서를 확인할 수 있습니다:

```
http://localhost:9090/swagger-ui.html
```

### 주요 엔드포인트

**인증**
- `POST /api/v1/users/signup` - 회원가입
- `POST /api/v1/users/login` - 로그인
- `POST /api/v1/users/refresh` - 토큰 갱신
- `GET /api/v1/users/profile` - 프로필 조회

**시나리오**
- `GET /api/v1/scenarios` - 전체 시나리오 조회
- `GET /api/v1/scenarios/default` - 기본 시나리오 조회
- `POST /api/v1/scenarios` - 커스텀 시나리오 생성

**세션**
- `POST /api/v1/sessions` - 대화 세션 생성
- `GET /api/v1/sessions/{sessionId}` - 세션 조회
- `PUT /api/v1/sessions/{sessionId}/complete` - 세션 종료

**피드백**
- `POST /api/v1/feedbacks/sessions/{sessionId}` - AI 피드백 생성
- `GET /api/v1/feedbacks/{sessionId}` - 피드백 조회
- `GET /api/v1/feedbacks/users/{userId}/history` - 피드백 히스토리

**실시간 대화**
- `POST /api/v1/realtime/session` - Ephemeral Key 발급

## 스크립트

```bash
./gradlew build        # 프로젝트 빌드
./gradlew bootRun      # 애플리케이션 실행
./gradlew test         # 테스트 실행
./gradlew clean        # 빌드 파일 정리
```

## 개발 가이드

### 프로파일

- **local**: 로컬 개발 환경 (기본값)
- **dev**: 개발 서버 환경
- **prod**: 운영 서버 환경

프로파일 변경:
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### 코드 스타일

- 패키지: 소문자 (com.aid.train.backend)
- 클래스: PascalCase (UserService)
- 메서드/변수: camelCase (getUserById)
- 상수: UPPER_SNAKE_CASE (MAX_RETRY_COUNT)

### 레이어 아키텍처

```
Controller → Service → Repository → Entity
     ↓          ↓
    DTO       DTO
```

## 라이선스

Copyright (c) 2025 Aid Team. All Rights Reserved.

이 프로젝트는 독점 소프트웨어입니다. 무단 사용, 복제, 수정 및 배포는 금지됩니다.

## 팀

Aid Team
- 왕택준
- 김경민
- 진도희

## 문의

- Email: dialogym.official@gmail.com
- Issues: GitHub Issues
