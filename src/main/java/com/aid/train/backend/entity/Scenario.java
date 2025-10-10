package com.aid.train.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
/**
 * 대화 시나리오 엔티티
 *
 * @author 진도희
 * @since 2025-10-08
 */
@Entity
@Table(name = "scenario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Voice { ONYX, ECHO, NOVA }
    public enum Difficulty { EASY, MEDIUM, HARD }
    public enum Category { WORK, RELATIONSHIP, FAMILY, FRIEND }
    public enum Status { DRAFT, PUBLISHED }
}
