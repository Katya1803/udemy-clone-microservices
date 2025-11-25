package com.app.course.entity;

import com.app.common.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "lecture_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"enrollment_id", "lecture_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_watched_position", nullable = false)
    @Builder.Default
    private Integer lastWatchedPosition = 0;
}