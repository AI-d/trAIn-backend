package com.aid.train.backend.entity.scenario;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name="Scenario")
@Entity
@Builder
@Getter
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

    // 유저, 대화세션 엔터티 연결 후 재 테스트 필요

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum Voice { ONYX, ECHO, NOVA }
    public enum Difficulty { EASY, MEDIUM, HARD }
    public enum Category { WORK, RELATIONSHIP, FAMILY, FRIEND }
    public enum Status { DRAFT, PUBLISHED }
}
