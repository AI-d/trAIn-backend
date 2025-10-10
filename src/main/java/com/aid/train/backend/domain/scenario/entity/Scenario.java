package com.aid.train.backend.domain.scenario.entity;

import com.aid.train.backend.domain.scenario.dto.request.ScenarioRequestDto;
import com.aid.train.backend.domain.session.entity.DialogueSession;
import com.aid.train.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.aid.train.backend.domain.user.entity.QUser.user;

@Table(name="Scenario")
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString()
public class Scenario
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Voice voice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Category category;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String locale = "ko-KR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private Status status = Status.PUBLISHED;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDefault = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DialogueSession> sessions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum Voice { ONYX, ECHO, NOVA }
    public enum Difficulty { EASY, MEDIUM, HARD }
    public enum Category { WORK, RELATIONSHIP, FAMILY, FRIEND }
    public enum Status { DRAFT, PUBLISHED }

    // dto -> entity 편의 메소드
    public static Scenario toEntity(ScenarioRequestDto dto, User user) {
        return Scenario.builder()
                .title(dto.title())
                .description(dto.description())
                .prompt(dto.prompt())
                .voice(dto.voice())
                .difficulty(dto.difficulty())
                .category(dto.category())
                .locale(dto.locale())
                .status(Status.DRAFT) // 서비스 단 비속어 검증 후 PUBLISHED 로 업데이트 -> db 저장, AI 연결요청 프롬프트 생성
                .isDefault(false) // 사용자 생성 시나리오 일 경우 false
                // 유저 연결 필요
                .owner(user)
                .build();
    }

    // 비속어 필터 검증 후 시나리오 상태 변경 편의 메소드
    public static void updateStatus(Scenario scenario) {
        scenario.setStatus(Status.PUBLISHED);
    }

}

