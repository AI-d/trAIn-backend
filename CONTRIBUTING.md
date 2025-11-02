# 기여 가이드 (Contributing Guide)

**Aid 팀**의 **trAIn 프로젝트 - Dialogym**에 기여해주셔서 감사합니다! 이 문서는 백엔드 프로젝트에 기여하는 방법을 안내합니다.

## 목차

- [행동 강령](#행동-강령)
- [시작하기](#시작하기)
- [개발 워크플로우](#개발-워크플로우)
- [브랜치 전략](#브랜치-전략)
- [커밋 컨벤션](#커밋-컨벤션)
- [Pull Request 가이드](#pull-request-가이드)
- [코드 스타일](#코드-스타일)
- [테스트](#테스트)

## 행동 강령

이 프로젝트는 모든 기여자가 존중받는 환경을 유지하기 위해 노력합니다. 다음 원칙을 준수해주세요:

- 서로를 존중하고 배려합니다
- 건설적인 피드백을 제공합니다
- 다양한 관점과 경험을 환영합니다
- 프로젝트와 커뮤니티의 이익을 최우선으로 합니다

## 시작하기

### 1. 저장소 포크 및 클론

```bash
# 저장소 포크 후 클론
git clone https://github.com/YOUR_USERNAME/trAIn-backend.git
cd trAIn-backend

# 원본 저장소를 upstream으로 추가
git remote add upstream https://github.com/AI-d/trAIn-backend.git
```

### 2. 개발 환경 설정

```bash
# 환경 변수 설정
copy .env.template .env
# .env 파일을 열어 필요한 값 설정

# 데이터베이스 생성
# MariaDB에 접속하여 실행:
# CREATE DATABASE dialogym CHARACTER SET utf8mb4 COLLATE utf8mb4nicode_ci;

# 빌드 및 실행
./gradlew bootRun
```

개발 서버는 `http://localhost:9090`에서 실행됩니다.

### 3. 최신 변경사항 동기화

```bash
git fetch upstream
git checkout main
git merge upstream/main
```

## 개발 워크플로우

### 1. 이슈 확인 또는 생성

- 기존 이슈를 확인하거나 새로운 이슈를 생성합니다
- 이슈 생성 시 [이슈 템플릿](.github/ISSUE_TEMPLATE/)을 참고해주세요
- 작업하기 전에 이슈에 코멘트를 남겨 중복 작업을 방지합니다

### 2. 브랜치 생성

```bash
# 최신 main 브랜치에서 시작
git switch main
git pull upstream main

# 새 브랜치 생성
git switch -b feature/your-feature-name
```

### 3. 개발 및 테스트

- 코드를 작성하고 테스트합니다
- 빌드가 성공하는지 확인합니다

```bash
# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 코드 포맷 확인
./gradlew checkstyleMain
```

### 4. 커밋

```bash
git add .
git commit -m "feat: 새로운 기능 추가"
```

### 5. Push 및 Pull Request

```bash
git push origin feature/your-feature-name
```

GitHub에서 Pull Request를 생성합니다.

## 브랜치 전략

### 브랜치 명명 규칙

- `feature/기능명` - 새로운 기능 개발
- `fix/버그명` - 버그 수정
- `docs/문서명` - 문서 작업
- `refactor/리팩토링명` - 코드 리팩토링
- `style/스타일명` - 코드 스타일 변경 (포맷팅 등)
- `test/테스트명` - 테스트 추가 또는 수정
- `chore/작업명` - 빌드 프로세스, 도구 설정 등

### 예시

```bash
feature/add-oauth-login
fix/jwt-token-expiration
docs/update-api-documentation
refactor/simplify-feedback-service
style/format-controllers
test/add-session-integration-tests
chore/update-spring-boot-version
```

## 커밋 컨벤션

[Conventional Commits](https://www.conventionalcommits.org/) 규칙을 따릅니다.

### 커밋 메시지 형식

```
<타입>(<범위>): <제목>

<본문>

<푸터>
```

### 타입

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 변경
- `style`: 코드 포맷팅 (기능 변경 없음)
- `refactor`: 코드 리팩토링
- `test`: 테스트 추가 또는 수정
- `chore`: 빌드 프로세스, 도구 설정 등
- `perf`: 성능 개선

### 예시

```bash
feat(auth): OAuth2 소셜 로그인 기능 추가

Google, Kakao, Naver 소셜 로그인을 지원합니다.
Spring Security OAuth2 Client를 사용하여 구현했습니다.

Closes #123
```

```bash
fix(session): 세션 타임아웃 처리 오류 수정

세션이 만료되었을 때 적절한 예외를 반환하도록 수정했습니다.

Fixes #456
```

## Pull Request 가이드

### PR 생성

- PR 생성 시 [PR 템플릿](.github/PULL_REQUEST_TEMPLATE.md)이 자동으로 적용됩니다
- 템플릿의 모든 항목을 작성해주세요

### PR 체크리스트

PR을 생성하기 전에 다음 사항을 확인해주세요:

- [ ] 코드가 빌드됩니다 (`./gradlew build`)
- [ ] 모든 테스트가 통과합니다 (`./gradlew test`)
- [ ] 새로운 기능에 대한 API 문서를 추가했습니다
- [ ] 커밋 메시지가 컨벤션을 따릅니다
- [ ] 브랜치가 최신 main과 동기화되어 있습니다
- [ ] 데이터베이스 스키마 변경 시 마이그레이션 스크립트를 추가했습니다

### 리뷰 프로세스

1. PR을 생성하면 자동으로 빌드 및 테스트가 실행됩니다
2. 최소 1명의 리뷰어 승인이 필요합니다
3. 모든 코멘트가 해결되어야 합니다
4. CI/CD 체크가 모두 통과해야 합니다

## 코드 스타일

### Java/Spring Boot

- Java 17 기능을 적극 활용합니다
- Spring Boot 모범 사례를 따릅니다
- Lombok을 사용하여 보일러플레이트 코드를 줄입니다

### 레이어 아키텍처

```
Controller → Service → Repository → Entity
     ↓          ↓
   DTO        DTO
```

각 레이어의 책임을 명확히 분리합니다:

- **Controller**: HTTP 요청/응답 처리, 입력 검증
- **Service**: 비즈니스 로직 구현
- **Repository**: 데이터 접근 로직
- **Entity**: 데이터베이스 테이블 매핑

### 컨트롤러 작성 규칙

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    /**
     * 사용자 프로필 조회
     * @param userId 사용자 ID
     * @return 사용자 프로필 정보
     */
    @GetMapping("/{userId}")
    @Operation(summary = "사용자 프로필 조회", description = "사용자 ID로 프로필 정보를 조회합니다")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable Long userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
```

### 서비스 작성 규칙

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 프로필 조회
     * @param userId 사용자 ID
     * @return 사용자 프로필 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return UserProfileResponse.from(user);
    }

    @Transactional
    public void updateUserProfile(Long userId, UserUpdateRequest request) {
        // 트랜잭션이 필요한 메서드에만 @Transactional 추가
    }
}
```

### 엔티티 작성 규칙

```java
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Builder
    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }

    // 비즈니스 로직 메서드
    public void updateName(String name) {
        this.name = name;
    }
}
```

### 네이밍 컨벤션

- **패키지**: 소문자 (예: `com.aid.train.backend.domain.user`)
- **클래스**: PascalCase (예: `UserService`, `UserController`)
- **메서드/변수**: camelCase (예: `getUserById`, `userName`)
- **상수**: UPPER_SNAKE_CASE (예: `MAX_RETRY_COUNT`, `API_VERSION`)
- **테이블**: snake_case (예: `users`, `feedback_history`)

### DTO 작성 규칙

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileResponse {

    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;

    @Builder
    public UserProfileResponse(Long id, String email, String name, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
```

### 예외 처리

```java
@Getter
public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long userId) {
        super(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId);
    }
}
```

## 테스트

### 테스트 작성

```java
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getUserProfile_Success() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("테스트")
                .build();
        User savedUser = userRepository.save(user);

        // when
        UserProfileResponse profile = userService.getUserProfile(savedUser.getId());

        // then
        assertThat(profile.getEmail()).isEqualTo("test@example.com");
        assertThat(profile.getName()).isEqualTo("테스트");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
    void getUserProfile_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> userService.getUserProfile(nonExistentId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
```

### 컨트롤러 테스트

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("사용자 프로필 조회 API 테스트")
    void getUserProfile() throws Exception {
        // given
        Long userId = 1L;
        UserProfileResponse response = UserProfileResponse.builder()
                .id(userId)
                .email("test@example.com")
                .name("테스트")
                .build();

        when(userService.getUserProfile(userId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("테스트"));
    }
}
```

### 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests UserServiceTest

# 특정 테스트 메서드 실행
./gradlew test --tests UserServiceTest.getUserProfile_Success

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```

## API 문서화

Swagger/OpenAPI를 사용하여 API를 문서화합니다:

```java
@Operation(
    summary = "사용자 프로필 조회",
    description = "사용자 ID로 프로필 정보를 조회합니다"
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
    ),
    @ApiResponse(
        responseCode = "404",
        description = "사용자를 찾을 수 없음"
    )
})
@GetMapping("/{userId}")
public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId) {
    // ...
}
```

## 데이터베이스 마이그레이션

- JPA DDL Auto는 개발 환경에서만 사용합니다
- 운영 환경에서는 수동 마이그레이션 스크립트를 작성합니다
- 스키마 변경 시 `src/main/resources/db/migration/` 디렉토리에 SQL 파일을 추가합니다

## 추가 리소스

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Data JPA 문서](https://spring.io/projects/spring-data-jpa)
- [Spring Security 문서](https://spring.io/projects/spring-security)
- [프로젝트 문서](https://github.com/AI-d/Dialogym-docs/tree/dev/docs)

## 질문이나 도움이 필요하신가요?

- GitHub Issues에 질문을 올려주세요
- 이메일: dialogym.official@gmail.com

## 감사합니다

여러분의 기여가 Aid 팀의 trAIn 프로젝트 Dialogym을 더 나은 서비스로 만듭니다. 감사합니다!

